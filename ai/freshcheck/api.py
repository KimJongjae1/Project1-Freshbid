import cv2
import io
import numpy as np
import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
from keras.models import load_model
from PIL import Image
from flask import Flask, request, jsonify
from flask_cors import CORS


app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Test model loading at startup
print("Loading models...")
try:
    # Test loading freshness model
    test_model = load_model('rottenvsfresh98pval.h5')
    print("✓ Freshness model loaded successfully")
except Exception as e:
    print(f"✗ Failed to load freshness model: {e}")

try:
    # Test loading classification model
    test_class_model = models.mobilenet_v2(weights=None)
    test_class_model.classifier[1] = nn.Linear(test_class_model.last_channel, 36)
    test_class_model.load_state_dict(torch.load('modelforclass.pth', map_location=torch.device('cpu')))
    print("✓ Classification model loaded successfully")
except Exception as e:
    print(f"✗ Failed to load classification model: {e}")

print("Flask app starting...")

# Classify fresh/rotten fn
def ret_fresh(res):
    threshold_fresh = 0.90  # set according to standards
    threshold_medium = 0.50  # set according to standards
    if res > threshold_fresh:
        return "The item is VERY FRESH!"
    elif threshold_fresh > res > threshold_medium:
        return "The item is FRESH"
    else:
        return "The item is NOT FRESH"
    

def pre_proc_img(image_data):
    # Convert the JpegImageFile object to bytes
    byte_stream = io.BytesIO()
    image_data = image_data.convert('RGB')  # Convert to RGB
    image_data.save(byte_stream, format='JPEG')
    image_bytes = byte_stream.getvalue()

    # img data to a np arr and read using cv2
    img_array = np.frombuffer(image_bytes, dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    # Check if image decoding was successful
    if img is None:
        raise ValueError("Image decoding failed. Please check the input image format.")

    # Cnvrt BGR to RGB & resize
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, (100, 100))

    # Preprocess the image
    img = img / 255.0
    img = np.expand_dims(img, axis=0)
    return img

def evaluate_rotten_vs_fresh(image_path):
    # Load and predict using the model
    model_path = 'rottenvsfresh98pval.h5'
    try:
        model = load_model(model_path)
    except Exception as e:
        raise RuntimeError(f"Failed to load model from {model_path}: {e}")

    if model is None or not hasattr(model, 'predict'):
        raise AttributeError("Loaded model is None or does not have a 'predict' method. Check the model file and loading process.")

    prediction = model.predict(pre_proc_img(image_path))

    return prediction[0][0]


def ident_type(img): #identify type of fruit/veg using pytorch
    try:
        # Convert RGBA to RGB if needed
        if img.mode == 'RGBA':
            img = img.convert('RGB')
        elif img.mode != 'RGB':
            img = img.convert('RGB')
            
        # Load the pretrained model
        model = models.mobilenet_v2(weights=None)
        num_classes = 36  # Update with the number of classes in your model
        model.classifier[1] = nn.Linear(model.last_channel, num_classes)
        model.load_state_dict(torch.load('modelforclass.pth', map_location=torch.device('cpu')))
        model.eval()

        # Define the data transforms and class labels
        transform = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])
        class_labels = ['apple', 'banana', 'beetroot', 'bell pepper', 'cabbage', 'capsicum', 'carrot', 'cauliflower',
                        'chilli pepper', 'corn', 'cucumber', 'eggplant', 'garlic', 'ginger', 'grapes', 'jalepeno', 'kiwi',
                        'lemon', 'lettuce', 'mango', 'onion', 'orange', 'paprika', 'pear', 'peas', 'pineapple', 'pomegranate',
                        'potato', 'raddish', 'soy beans', 'spinach', 'sweetcorn', 'sweetpotato', 'tomato', 'turnip',
                        'watermelon']
        with torch.no_grad():
            img = transform(img)
            img = img.unsqueeze(0)
            output = model(img)

        probabilities = torch.softmax(output, 1)
        max_prob, predicted_idx = torch.max(probabilities, 1)
        
        if max_prob.item() < 0.3:  # confidence threshold
            return "unknown"
        
        predicted_label = class_labels[int(predicted_idx.item())]
        return predicted_label
    except Exception as e:
        return "unknown"


@app.route('/')
def home():
    return jsonify({'message': 'Freshness Detection API is running!', 'endpoints': ['/classify (POST)'], 'status': 'OK'})

@app.route('/health')
def health():
    return jsonify({'status': 'healthy', 'message': 'API is running normally'})

@app.route('/classify', methods=['GET'])
def classify_info():
    return jsonify({'message': 'Send POST request with image file to classify freshness', 'method': 'POST', 'parameter': 'image', 'example': 'curl -X POST -F "image=@your_image.jpg" http://localhost:6000/classify'})

@app.route('/classify', methods=['POST'])
def classify():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image found'})

        image = request.files['image']
        img = Image.open(image.stream)
        
        # Convert to RGB if needed
        if img.mode == 'RGBA':
            img = img.convert('RGB')
        elif img.mode != 'RGB':
            img = img.convert('RGB')
            
        pred_type = ident_type(img)
        
        if pred_type == "unknown":
            return jsonify({'prediction': 'N/A', 'freshness': 'Cannot determine freshness for unrecognized items', 'type': 'unknown'})
        
        is_fresh = 1 - evaluate_rotten_vs_fresh(img)
        return jsonify({'prediction': str(is_fresh), 'freshness':ret_fresh(is_fresh), 'type':pred_type})
    except Exception as e:
        return jsonify({'error': f'Processing failed: {str(e)}'}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
import React from 'react';

interface GestureGuideModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const GestureGuideModal: React.FC<GestureGuideModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  const gestures = [
    { number: 1, name: 'one', description: '검지 손가락 하나', amount: '1,000원 추가' },
    { number: 2, name: 'two', description: '검지와 중지 두 개', amount: '2,000원 추가' },
    { number: 3, name: 'three', description: '검지, 중지, 약지 세 개', amount: '3,000원 추가' },
    { number: 4, name: 'four', description: '검지, 중지, 약지, 소지 네 개', amount: '4,000원 추가' },
    { number: 5, name: 'five', description: '모든 손가락 펼치기', amount: '5,000원 추가' },
    { number: 6, name: 'six', description: '엄지와 검지로 6 모양', amount: '6,000원 추가' },
    { number: 7, name: 'seven', description: '검지와 중지로 7 모양', amount: '7,000원 추가' },
    { number: 8, name: 'eight', description: '검지와 중지로 8 모양', amount: '8,000원 추가' },
    { number: 9, name: 'nine', description: '검지와 중지로 9 모양', amount: '9,000원 추가' },
    { number: 10, name: 'ten', description: '주먹 쥐기', amount: '10,000원 추가' },
  ];

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000,
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '20px',
        padding: '32px',
        maxWidth: '600px',
        width: '100%',
        maxHeight: '90vh',
        overflow: 'auto',
        boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
        border: '1px solid #e5e7eb'
      }}>
        {/* 헤더 */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px',
          paddingBottom: '16px',
          borderBottom: '2px solid #f3f4f6'
        }}>
          <h2 style={{
            fontSize: '24px',
            fontWeight: '700',
            color: '#1f2937',
            margin: 0,
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            <span style={{ fontSize: '28px' }}>🤚</span>
            수신호 제스처 가이드
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#6b7280',
              padding: '8px',
              borderRadius: '8px',
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#f3f4f6';
              e.currentTarget.style.color = '#374151';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = 'transparent';
              e.currentTarget.style.color = '#6b7280';
            }}
          >
            ✕
          </button>
        </div>

        {/* 제스처 이미지 */}
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          marginBottom: '24px'
        }}>
          <img 
            src="/guideline/handsignal.png" 
            alt="수신호 제스처 가이드" 
            style={{
              maxWidth: '100%',
              height: 'auto',
              borderRadius: '12px',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
              border: '2px solid #e5e7eb'
            }}
          />
        </div>

        {/* 설명 */}
        <div style={{
          backgroundColor: '#f0fdf4',
          padding: '16px',
          borderRadius: '12px',
          marginBottom: '24px',
          border: '1px solid #bbf7d0'
        }}>
          <h3 style={{
            fontSize: '16px',
            fontWeight: '600',
            color: '#15803d',
            margin: '0 0 8px 0'
          }}>
            📋 사용 방법
          </h3>
          <ul style={{
            margin: '0',
            paddingLeft: '20px',
            color: '#15803d',
            fontSize: '14px',
            lineHeight: '1.5'
          }}>
            <li>카메라 앞에서 원하는 제스처를 1.5초간 유지하세요</li>
            <li>제스처 숫자 × 1,000원이 현재 최고가에 추가됩니다</li>
            <li>손을 카메라 화면 안에 명확히 보이도록 해주세요</li>
          </ul>
        </div>

        {/* 제스처 목록 */}
        <div style={{ marginBottom: '24px' }}>
          <h3 style={{
            fontSize: '18px',
            fontWeight: '600',
            color: '#1f2937',
            margin: '0 0 16px 0'
          }}>
            🎯 지원하는 제스처
          </h3>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '12px'
          }}>
            {gestures.map((gesture) => (
              <div key={gesture.number} style={{
                backgroundColor: '#f9fafb',
                padding: '16px',
                borderRadius: '12px',
                border: '1px solid #e5e7eb',
                transition: 'all 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = '#f3f4f6';
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.1)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = '#f9fafb';
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
              >
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginBottom: '8px'
                }}>
                  <span style={{
                    fontSize: '20px',
                    fontWeight: '700',
                    color: '#ffffff',
                    backgroundColor: '#22c55e',
                    padding: '4px 12px',
                    borderRadius: '20px',
                    minWidth: '40px',
                    textAlign: 'center'
                  }}>
                    {gesture.number}
                  </span>
                  <span style={{
                    fontSize: '14px',
                    fontWeight: '600',
                    color: '#059669',
                    backgroundColor: '#d1fae5',
                    padding: '4px 8px',
                    borderRadius: '6px'
                  }}>
                    {gesture.amount}
                  </span>
                </div>
                <p style={{
                  fontSize: '14px',
                  color: '#374151',
                  margin: '0',
                  fontWeight: '500'
                }}>
                  {gesture.description}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* 주의사항 */}
        <div style={{
          backgroundColor: '#fef3c7',
          padding: '16px',
          borderRadius: '12px',
          border: '1px solid #fde68a'
        }}>
          <h4 style={{
            fontSize: '16px',
            fontWeight: '600',
            color: '#92400e',
            margin: '0 0 8px 0',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span>⚠️</span>
            주의사항
          </h4>
          <ul style={{
            margin: '0',
            paddingLeft: '20px',
            color: '#92400e',
            fontSize: '14px',
            lineHeight: '1.5'
          }}>
            <li>조명이 충분한 환경에서 사용해주세요</li>
            <li>손이 카메라에 명확히 보이도록 해주세요</li>
            <li>제스처를 정확하게 인식하지 못할 수 있습니다</li>
            <li>입찰 전 반드시 금액을 확인해주세요</li>
          </ul>
        </div>

        {/* 닫기 버튼 */}
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          marginTop: '24px',
          paddingTop: '16px',
          borderTop: '1px solid #e5e7eb'
        }}>
          <button
            onClick={onClose}
            style={{
              backgroundColor: '#22c55e',
              color: 'white',
              border: 'none',
              padding: '12px 32px',
              borderRadius: '12px',
              fontSize: '16px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s ease',
              boxShadow: '0 4px 12px rgba(34, 197, 94, 0.3)'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#16a34a';
              e.currentTarget.style.transform = 'translateY(-2px)';
              e.currentTarget.style.boxShadow = '0 6px 16px rgba(34, 197, 94, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#22c55e';
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 4px 12px rgba(34, 197, 94, 0.3)';
            }}
          >
            확인했습니다
          </button>
        </div>
      </div>
    </div>
  );
};

export default GestureGuideModal;

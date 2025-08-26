import React from 'react';

const FreshCheckLoadingModal: React.FC = () => {
  return (
    <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
        <div className="bg-white p-6 rounded-md shadow-lg w-[400px] overflow-y-auto relative text-center">
            <p className="text-2xl font-semibold mb-4">AI 신선도 체크 중</p>
            <div className="w-15 h-15 ml-auto mr-auto border-4 border-gray-300 border-t-indigo-500 rounded-full animate-spin"></div>
        </div>
    </div>
  );
};

export default FreshCheckLoadingModal;

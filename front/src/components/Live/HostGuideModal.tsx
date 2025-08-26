import React, { useState } from 'react';

interface HostGuideModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const HostGuideModal: React.FC<HostGuideModalProps> = ({ isOpen, onClose }) => {
  const [isChecked, setIsChecked] = useState<boolean>(false);
  
  if (!isOpen) return null;

  return (
          <div
        style={{
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
        }}
      >
              <div
          style={{
            backgroundColor: 'white',
            borderRadius: '20px',
            padding: '32px',
            maxWidth: '520px',
            width: '90%',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            border: '1px solid #f3f4f6',
            position: 'relative',
            overflow: 'hidden',
          }}
          onClick={(e) => e.stopPropagation()}
        >
          {/* 상단 장식 요소 */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: '4px',
            background: 'linear-gradient(90deg, #10B981, #059669, #047857)',
          }} />
          
          <div style={{ marginBottom: '28px' }}>
            <h2 style={{ 
              margin: 0, 
              fontSize: '24px', 
              fontWeight: '700', 
              color: '#1f2937', 
              textAlign: 'center',
              letterSpacing: '-0.025em',
            }}>
              🎯 즐거운 경매에 참여하기 전 안내
            </h2>
          </div>
          
          <div style={{ 
            lineHeight: '1.7', 
            color: '#4b5563',
            backgroundColor: '#f9fafb',
            borderRadius: '12px',
            padding: '20px',
            marginBottom: '24px',
          }}>
            <ul style={{ 
              paddingLeft: '0', 
              margin: 0,
              listStyle: 'none',
            }}>
              <li style={{ 
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '12px',
              }}>
                                <span style={{
                  backgroundColor: '#10B981',
                  color: 'white',
                  borderRadius: '50%',
                  width: '20px',
                  height: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  flexShrink: 0,
                  marginTop: '4px',
                }}>
                  1
                </span>
                <span>경매 시작을 클릭하면 경매가 시작됩니다.</span>
              </li>
              <li style={{ 
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '12px',
              }}>
                                <span style={{
                  backgroundColor: '#10B981',
                  color: 'white',
                  borderRadius: '50%',
                  width: '20px',
                  height: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  flexShrink: 0,
                  marginTop: '4px',
                }}>
                  2
                </span>
                <span>같은 가격을 동시에 입찰한 경우 가장 먼저 입찰한 참가자에게 가격이 확정됩니다.</span>
              </li>
              <li style={{ 
                marginBottom: '0',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '12px',
              }}>
                                <span style={{
                  backgroundColor: '#10B981',
                  color: 'white',
                  borderRadius: '50%',
                  width: '20px',
                  height: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  flexShrink: 0,
                  marginTop: '4px',
                }}>
                  3
                </span>
                <span>낙찰 확정을 클릭하면 낙찰가가 확정되고 해당 경매가 종료됩니다.</span>
              </li>
               <li style={{ 
               marginBottom: '16px',
               display: 'flex',
               alignItems: 'flex-start',
               gap: '12px',
             }}></li>
                          <li style={{ 
                marginBottom: '0',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '12px',
              }}>
                                <span style={{
                  backgroundColor: '#10B981',
                  color: 'white',
                  borderRadius: '50%',
                  width: '20px',
                  height: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  flexShrink: 0,
                  marginTop: '4px',
                }}>
                  4
                </span>
                <span>낙찰 확정은 취소 불가능하니 신중하게 선택해주세요!</span>
              </li>
            </ul>
          </div>
          
          <div style={{ 
            marginBottom: '28px', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            backgroundColor: '#f0fdf4',
            borderRadius: '12px',
            padding: '16px',
            border: '2px solid #dcfce7',
          }}>
            <input
              type="checkbox"
              id="guide-checkbox"
              checked={isChecked}
              onChange={(e) => setIsChecked(e.target.checked)}
              style={{
                width: '20px',
                height: '20px',
                marginRight: '12px',
                cursor: 'pointer',
                accentColor: '#10B981',
              }}
            />
            <label
              htmlFor="guide-checkbox"
              style={{
                color: '#166534',
                fontSize: '16px',
                fontWeight: '600',
                cursor: 'pointer',
                userSelect: 'none',
              }}
            >
              안내 내용을 모두 확인했습니다
            </label>
          </div>
          
          <div style={{ textAlign: 'center' }}>
            <button
              onClick={onClose}
              disabled={!isChecked}
              style={{
                backgroundColor: isChecked ? '#10B981' : '#d1d5db',
                color: 'white',
                border: 'none',
                padding: '16px 32px',
                borderRadius: '12px',
                fontSize: '16px',
                fontWeight: '600',
                cursor: isChecked ? 'pointer' : 'not-allowed',
                minWidth: '140px',
                transition: 'all 0.2s ease',
                boxShadow: isChecked ? '0 4px 14px 0 rgba(16, 185, 129, 0.3)' : 'none',
              }}
              onMouseOver={(e) => {
                if (isChecked) {
                  e.currentTarget.style.backgroundColor = '#059669';
                  e.currentTarget.style.transform = 'translateY(-1px)';
                  e.currentTarget.style.boxShadow = '0 6px 20px 0 rgba(16, 185, 129, 0.4)';
                }
              }}
              onMouseOut={(e) => {
                if (isChecked) {
                  e.currentTarget.style.backgroundColor = '#10B981';
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = '0 4px 14px 0 rgba(16, 185, 129, 0.3)';
                }
              }}
            >
              {isChecked ? '🎉 경매 참여하기' : '확인해주세요'}
            </button>
          </div>
        </div>
     </div>
  );
};

export default HostGuideModal;

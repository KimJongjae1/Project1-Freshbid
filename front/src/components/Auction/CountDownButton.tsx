import { useState, useEffect } from 'react'
import dayjs from 'dayjs'
import { useUserStore } from '../../stores/useUserStore'
import axiosInstance from '../../api/axiosInstance'
import axios from 'axios'

type CounterProps = {
  auctionDate: string
  status: 'scheduled' | 'active' | 'closed'
  auctionId: number
  sellerId?: number
}

export default function CountDownButton({ auctionDate, status, auctionId }: CounterProps) {
  const [remaining, setRemaining] = useState<number>(dayjs(auctionDate).diff(dayjs(), 'second'))
  const { isLoggedIn } = useUserStore()
  const [isChecking, setIsChecking] = useState(false)

  useEffect(() => {
    const interval = setInterval(() => {
      setRemaining(dayjs(auctionDate).diff(dayjs(), 'second'))
    }, 1000)
    return () => clearInterval(interval)
  }, [auctionDate])

  const formatTime = (sec: number) => {
    if (sec <= 0) return '00:00:00'
    const d = Math.floor(sec / 86400)
    const h = Math.floor((sec % 86400) / 3600)
    const m = Math.floor((sec % 3600) / 60)
    const s = sec % 60
    return d > 0
      ? `${d}일 ${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
      : `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }

  const isBeforeStart = remaining > 0
  const isWaitingToStart = status === 'scheduled' && remaining <= 0
  const isLive = !isBeforeStart && status === 'active'
  const isEnded = status === 'closed'

  const buttonStyle = isLive
    ? 'bg-red-500 hover:bg-red-600 text-white'
    : isEnded
    ? 'bg-green-200 text-green-800 cursor-not-allowed'
    : 'bg-gray-300 text-white'

  const buttonText = isEnded
    ? '완료'
    : isBeforeStart
    ? `경매 시작 ${formatTime(remaining)}`
    : isWaitingToStart
    ? '시작 대기 중'
    : isChecking
    ? '확인 중...'
    : '라이브 입장'

  const go = (isOwner: boolean) => {
    const targetUrl = `/webrtc/${auctionId}?type=${isOwner ? 'host' : 'participant'}`
    window.location.assign(targetUrl)
  }

  // host 확인
  const checkOwnership = async () => {
    if (!isLoggedIn) {
      alert('로그인하고 경매에 참여해보세요!')
      return
    }
    if (!isLive) {
      return
    }

    try {
      setIsChecking(true)

      const res = await axiosInstance.get(`/auction/live/${auctionId}/ownership`)
      const isOwner = Boolean(res?.data?.data)
      go(isOwner)
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const sc = err.response?.status

        // 401/403/404 등 권한/접근 불가 계열은 "host 아님"으로 간주하고 참가자로 보냄
        if (sc === 401 || sc === 403 || sc === 404) {
          console.log('➡️ 권한/접근 불가 → 참가자로 폴백 이동')
          go(false)
          return
        }
      } else {
        console.error('❌ 예기치 못한 오류', err)
      }
      alert('입장에 실패했어요. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsChecking(false)
    }
  }

  // 클릭 시에 권한 판별
  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (isChecking) return
    checkOwnership()
  }

  return (
    <a
    // 링크 이동을 undefined로 막고 handleclick에서 제어
      href={undefined}
      className={`block text-center px-4 py-2 rounded-full text-base font-semibold transition-colors mt-3 ${buttonStyle} ${
        isChecking ? 'opacity-80 cursor-wait' : ''
      }`}
      onClick={handleClick}
      aria-disabled={!isLive || isChecking}
    >
      {buttonText}
    </a>
  )
}
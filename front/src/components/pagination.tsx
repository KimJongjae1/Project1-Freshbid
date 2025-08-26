import type { PaginationProps } from "../types/pagination"

export interface PaginationPropsWithHooks extends PaginationProps {
  goToPrevBlock?: () => void
  goToNextBlock?: () => void
  hasPrevBlock?: boolean
  hasNextBlock?: boolean
  getVisiblePages?: (blockSize?: number) => number[]
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  blockSize = 10,
  showFirstLast = true,
  showPrevNext = true,
  className = "",
  disabled = false,
  goToPrevBlock,
  goToNextBlock,
  hasPrevBlock,
  hasNextBlock,
  getVisiblePages
}: PaginationPropsWithHooks) {
  if (totalPages <= 1) {
    return null
  }

  // 훅에서 제공된 함수가 있으면 사용, 없으면 fallback 로직
  const getVisiblePagesFallback = (): number[] => {
    const currentBlock = Math.floor((currentPage - 1) / blockSize)
    const blockStart = currentBlock * blockSize + 1
    const blockEnd = Math.min(blockStart + blockSize - 1, totalPages)

    return Array.from({ length: blockEnd - blockStart + 1 }, (_, i) => blockStart + i)
  }

  // 블록 이동 fallback 로직
  const currentBlock = Math.floor((currentPage - 1) / blockSize)
  const prevBlockLastPage = currentBlock * blockSize
  const nextBlockFirstPage = (currentBlock + 1) * blockSize + 1
  const fallbackHasPrevBlock = currentBlock > 0
  const fallbackHasNextBlock = nextBlockFirstPage <= totalPages

  const visiblePages = getVisiblePages ? getVisiblePages(blockSize) : getVisiblePagesFallback()
  const actualHasPrevBlock = hasPrevBlock !== undefined ? hasPrevBlock : fallbackHasPrevBlock
  const actualHasNextBlock = hasNextBlock !== undefined ? hasNextBlock : fallbackHasNextBlock

  const baseButtonClass = `
    px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200
    border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 
    focus:ring-blue-500 focus:ring-offset-1 cursor-pointer
  `.trim()

  const activeButtonClass = `
    px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200
    bg-green-500 text-white border-green-500 hover:bg-green-600 cursor-pointer
    focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-1
  `.trim()

  const disabledButtonClass = `
    opacity-50 cursor-not-allowed hover:bg-transparent
  `.trim()

  const handleClick = (page: number) => {
    if (!disabled && page !== currentPage && page >= 1 && page <= totalPages) {
      onPageChange(page)
    }
  }

  const handlePrevBlockClick = () => {
    if (goToPrevBlock) {
      goToPrevBlock()
    } else if (!disabled && prevBlockLastPage > 0) {
      onPageChange(prevBlockLastPage)
    }
  }

  const handleNextBlockClick = () => {
    if (goToNextBlock) {
      goToNextBlock()
    } else if (!disabled && nextBlockFirstPage <= totalPages) {
      onPageChange(nextBlockFirstPage)
    }
  }

  return (
    <nav 
      className={`flex items-center justify-center space-x-1 ${className}`}
      aria-label="페이지네이션"
    >
      {showFirstLast && actualHasPrevBlock && (
        <button
          onClick={handlePrevBlockClick}
          disabled={disabled}
          className={`
            ${baseButtonClass} 
            ${disabled ? disabledButtonClass : ''}
          `}
          aria-label="이전 블록의 마지막 페이지로 이동"
        >
          ≪
        </button>
      )}

      {showPrevNext && currentPage > 1 && (
        <button
          onClick={() => handleClick(currentPage - 1)}
          disabled={disabled}
          className={`
            ${baseButtonClass} 
            ${disabled ? disabledButtonClass : ''}
          `}
          aria-label="이전 페이지로 이동"
        >
          ‹
        </button>
      )}

      {visiblePages.map((page) => (
        <button
          key={page}
          onClick={() => handleClick(page)}
          disabled={disabled}
          className={`
            ${page === currentPage ? activeButtonClass : baseButtonClass}
            ${disabled ? disabledButtonClass : ''}
          `}
          aria-label={`${page}페이지로 이동`}
          aria-current={page === currentPage ? 'page' : undefined}
        >
          {page}
        </button>
      ))}

      {showPrevNext && currentPage < totalPages && (
        <button
          onClick={() => handleClick(currentPage + 1)}
          disabled={disabled}
          className={`
            ${baseButtonClass} 
            ${disabled ? disabledButtonClass : ''}
          `}
          aria-label="다음 페이지로 이동"
        >
          ›
        </button>
      )}

      {showFirstLast && actualHasNextBlock && (
        <button
          onClick={handleNextBlockClick}
          disabled={disabled}
          className={`
            ${baseButtonClass} 
            ${disabled ? disabledButtonClass : ''}
          `}
          aria-label="다음 블록의 첫 페이지로 이동"
        >
          ≫
        </button>
      )}
    </nav>
  )
}

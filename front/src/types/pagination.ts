export interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  blockSize?: number
  showFirstLast?: boolean
  showPrevNext?: boolean
  className?: string
  disabled?: boolean
}

export interface PaginationState {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface PaginationOptions {
  initialPage?: number
  pageSize?: number
  blockSize?: number
  enableUrlSync?: boolean
}

export interface PageInfo<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface PaginatedResponse<T> {
  data: PageInfo<T>
  success: boolean
  message?: string
}

export interface PaginationHookReturn {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  hasNext: boolean
  hasPrevious: boolean
  goToPage: (page: number) => void
  goToPrevious: () => void
  goToNext: () => void
  goToFirst: () => void
  goToLast: () => void
  goToPrevBlock: () => void
  goToNextBlock: () => void
  hasPrevBlock: boolean
  hasNextBlock: boolean
  getVisiblePages: (blockSize?: number) => number[]
}
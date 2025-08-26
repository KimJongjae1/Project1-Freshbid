import { useState, useCallback, useMemo, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import type { PaginationOptions, PaginationHookReturn } from "../types/pagination";

export function usePagination(
  totalElements: number,
  options: PaginationOptions = {}
): PaginationHookReturn {
  const {
    initialPage = 1,
    pageSize = 10,
    blockSize = 10,
    enableUrlSync = false
  } = options

  const [searchParams, setSearchParams] = useSearchParams()
  
  const [internalPage, setInternalPage] = useState(initialPage)
  
  const currentPage = useMemo(() => {
    if (enableUrlSync) {
      const pageParam = searchParams.get('page')
      return pageParam ? Math.max(1, parseInt(pageParam, 10)) : initialPage
    }
    return internalPage
  }, [enableUrlSync, searchParams, internalPage, initialPage])

  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize))
  const hasNext = currentPage < totalPages
  const hasPrevious = currentPage > 1

  const updatePage = useCallback((newPage: number) => {
    const validPage = Math.max(1, Math.min(newPage, totalPages))
    
    if (enableUrlSync) {
      const newSearchParams = new URLSearchParams(searchParams)
      newSearchParams.set('page', validPage.toString())
      setSearchParams(newSearchParams)
    } else {
      setInternalPage(validPage)
    }
  }, [enableUrlSync, totalPages, searchParams, setSearchParams])

  const goToPage = useCallback((page: number) => {
    updatePage(page)
  }, [updatePage])

  const goToPrevious = useCallback(() => {
    if (hasPrevious) {
      updatePage(currentPage - 1)
    }
  }, [currentPage, hasPrevious, updatePage])

  const goToNext = useCallback(() => {
    if (hasNext) {
      updatePage(currentPage + 1)
    }
  }, [currentPage, hasNext, updatePage])

  const goToFirst = useCallback(() => {
    updatePage(1)
  }, [updatePage])

  const goToLast = useCallback(() => {
    updatePage(totalPages)
  }, [totalPages, updatePage])

  // 블록 단위 이동 (기본 10페이지)
  const currentBlock = Math.floor((currentPage - 1) / blockSize)
  
  const goToPrevBlock = useCallback(() => {
    const prevBlockLastPage = currentBlock * blockSize
    if (prevBlockLastPage > 0) {
      updatePage(prevBlockLastPage)
    }
  }, [currentBlock, blockSize, updatePage])

  const goToNextBlock = useCallback(() => {
    const nextBlockFirstPage = (currentBlock + 1) * blockSize + 1
    if (nextBlockFirstPage <= totalPages) {
      updatePage(nextBlockFirstPage)
    }
  }, [currentBlock, blockSize, totalPages, updatePage])

  const hasPrevBlock = currentBlock > 0
  const hasNextBlock = (currentBlock + 1) * blockSize + 1 <= totalPages

  const getVisiblePages = useCallback((visibleBlockSize: number = blockSize): number[] => {
    const block = Math.floor((currentPage - 1) / visibleBlockSize)
    const blockStart = block * visibleBlockSize + 1
    const blockEnd = Math.min(blockStart + visibleBlockSize - 1, totalPages)

    return Array.from({ length: blockEnd - blockStart + 1 }, (_, i) => blockStart + i)
  }, [currentPage, totalPages, blockSize])

  useEffect(() => {
    if (currentPage > totalPages && totalPages > 0) {
      updatePage(totalPages)
    }
  }, [currentPage, totalPages, updatePage])

  return {
    currentPage,
    totalPages,
    totalElements,
    pageSize,
    hasNext,
    hasPrevious,
    goToPage,
    goToPrevious,
    goToNext,
    goToFirst,
    goToLast,
    goToPrevBlock,
    goToNextBlock,
    hasPrevBlock,
    hasNextBlock,
    getVisiblePages
  }
}
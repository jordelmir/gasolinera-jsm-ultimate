package com.gasolinerajsm.coupon.repository

import com.gasolinerajsm.coupon.domain.CouponStatus
import com.gasolinerajsm.coupon.domain.QRCoupon
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface QRCouponRepository : JpaRepository<QRCoupon, UUID> {

    fun findByQrCode(qrCode: String): QRCoupon?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM QRCoupon c WHERE c.qrCode = :qrCode")
    fun findAndLockByQrCode(@Param("qrCode") qrCode: String): QRCoupon?

    fun findByToken(token: String): QRCoupon?

    fun findByScannedBy(userId: UUID, pageable: Pageable): Page<QRCoupon>

    fun findByStationId(stationId: UUID): List<QRCoupon>

    fun findByEmployeeId(employeeId: UUID): List<QRCoupon>

    fun findByStatus(status: CouponStatus): List<QRCoupon>

    @Query("SELECT c FROM QRCoupon c WHERE c.scannedBy = :userId AND c.status IN :statuses")
    fun findByUserIdAndStatuses(
        @Param("userId") userId: UUID,
        @Param("statuses") statuses: List<CouponStatus>
    ): List<QRCoupon>

    @Query("SELECT c FROM QRCoupon c WHERE c.expiresAt < :now AND c.status NOT IN ('EXPIRED', 'USED_IN_RAFFLE')")
    fun findExpiredCoupons(@Param("now") now: LocalDateTime): List<QRCoupon>

    @Query("SELECT c FROM QRCoupon c WHERE c.status = 'COMPLETED' AND c.scannedBy IS NOT NULL")
    fun findActiveTicketsForRaffle(): List<QRCoupon>

    @Query("""
        SELECT
            COUNT(c) as totalCoupons,
            COALESCE(SUM(c.totalTickets), 0) as totalTickets,
            COUNT(CASE WHEN c.status IN ('ACTIVATED', 'COMPLETED') THEN 1 END) as activeCoupons,
            COUNT(CASE WHEN c.status = 'EXPIRED' THEN 1 END) as expiredCoupons
        FROM QRCoupon c
        WHERE c.stationId = :stationId
    """)
    fun getStationStatsOptimized(@Param("stationId") stationId: UUID): Array<Any>

    @Query("""
        SELECT
            COUNT(c) as totalCoupons,
            COALESCE(SUM(c.totalTickets), 0) as totalTickets,
            COUNT(c.scannedBy) as scannedCoupons,
            CASE WHEN COUNT(c) > 0 THEN
                (CAST(COUNT(c.scannedBy) AS double) / COUNT(c) * 100)
            ELSE 0.0 END as conversionRate
        FROM QRCoupon c
        WHERE c.employeeId = :employeeId
    """)
    fun getEmployeeStatsOptimized(@Param("employeeId") employeeId: UUID): Array<Any>
}
package com.gasolinerajsm.coupon.repository

import com.gasolinerajsm.coupon.domain.CouponStatus
import com.gasolinerajsm.coupon.domain.QRCoupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface QRCouponRepository : JpaRepository<QRCoupon, UUID> {

    fun findByQrCode(qrCode: String): QRCoupon?

    fun findByToken(token: String): QRCoupon?

    fun findByScannedBy(userId: UUID): List<QRCoupon>

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
        SELECT c.stationId, COUNT(c) as total, SUM(c.totalTickets) as tickets
        FROM QRCoupon c
        WHERE c.createdAt BETWEEN :startDate AND :endDate
        GROUP BY c.stationId
    """)
    fun getStationStats(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>

    @Query("""
        SELECT c.employeeId, COUNT(c) as total, SUM(c.totalTickets) as tickets
        FROM QRCoupon c
        WHERE c.createdAt BETWEEN :startDate AND :endDate
        GROUP BY c.employeeId
    """)
    fun getEmployeeStats(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>
}
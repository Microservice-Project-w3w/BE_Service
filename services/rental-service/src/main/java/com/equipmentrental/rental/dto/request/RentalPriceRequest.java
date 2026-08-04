package com.equipmentrental.rental.dto;

import com.equipmentrental.rental.entity.RentalUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalPriceRequest(

        @NotBlank(message = "Tên bảng giá không được để trống")
        @Size(max = 150)
        String priceName,

        @NotNull(message = "Mã loại thiết bị không được để trống")
        Long equipmentTypeId,

        @NotNull(message = "Đơn vị thuê không được để trống")
        RentalUnit rentalUnit,

        @NotNull(message = "Giá thuê không được để trống")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal rentalPrice,

        @NotNull(message = "Tiền đặt cọc không được để trống")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal depositAmount,

        @NotNull(message = "Phí trả trễ không được để trống")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal lateFee,

        @NotNull(message = "Ngày bắt đầu không được để trống")
        LocalDateTime validFrom,

        LocalDateTime validTo,

        Boolean active,

        @Size(max = 500)
        String description
) {
}
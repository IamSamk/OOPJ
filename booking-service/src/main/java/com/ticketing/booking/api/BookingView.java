package com.ticketing.booking.api;

import com.ticketing.common.dto.BookingResponse;
import com.ticketing.common.dto.PaymentIntentResponse;

public record BookingView(BookingResponse booking, PaymentIntentResponse paymentIntent) {
}

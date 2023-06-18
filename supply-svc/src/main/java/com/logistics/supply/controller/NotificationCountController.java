package com.logistics.supply.controller;

import com.logistics.supply.dto.NotificationDataDTO;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.service.NotificationDataService;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationCountController {
  private final NotificationDataService notificationDataService;

  @GetMapping("/notifications")
  public ResponseEntity<?> getNotificationData(Authentication authentication, Pageable pageable) {
    if (authentication == null) return Helper.failedResponse("Session expired, kindly login");
    NotificationDataDTO data =
        notificationDataService.getNotificationData(authentication, pageable);
    return ResponseDto.wrapSuccessResult(data, FETCH_SUCCESSFUL);
  }
}

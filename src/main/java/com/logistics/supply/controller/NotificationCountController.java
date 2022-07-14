package com.logistics.supply.controller;

import com.logistics.supply.dto.NotificationDataDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.service.NotificationDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Helper.failedResponse;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationCountController {
  private final NotificationDataService notificationDataService;

  @GetMapping("/notifications")
  public ResponseEntity<?> getNotificationData(Authentication authentication, Pageable pageable) {
    if (authentication == null) return failedResponse("Session expired, kindly login");
    NotificationDataDTO data =
        notificationDataService.getNotificationData(authentication, pageable);
    return ResponseDTO.wrapSuccessResult(data, FETCH_SUCCESSFUL);
  }
}

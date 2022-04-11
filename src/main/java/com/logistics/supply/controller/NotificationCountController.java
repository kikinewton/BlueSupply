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

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationCountController {
  private final NotificationDataService notificationDataService;

  @GetMapping("/notifications")
  public ResponseEntity<?> getNotificationData(Authentication authentication, Pageable pageable) {
    if (authentication == null) return failedResponse("Auth required");
    NotificationDataDTO data =
        notificationDataService.getNotificationData(authentication, pageable);
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, data);
    if (data != null) return ResponseEntity.ok(response);
    return notFound("NO DATA FOUND");
  }
}

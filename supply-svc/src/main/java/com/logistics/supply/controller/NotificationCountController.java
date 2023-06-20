package com.logistics.supply.controller;

import com.logistics.supply.dto.NotificationDataDto;
import com.logistics.supply.dto.ResponseDto;
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

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationCountController {
  private final NotificationDataService notificationDataService;

  @GetMapping("/notifications")
  public ResponseEntity<ResponseDto<NotificationDataDto>> getNotificationData(
          Authentication authentication,
          Pageable pageable) {

    log.info("Fetch notification data for user {}", authentication.getName());
    NotificationDataDto data =
        notificationDataService.getNotificationData(authentication, pageable);
    return ResponseDto.wrapSuccessResult(data, FETCH_SUCCESSFUL);
  }
}

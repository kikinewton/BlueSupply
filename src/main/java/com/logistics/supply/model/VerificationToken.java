package com.logistics.supply.model;

import com.logistics.supply.enums.VerificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private long id;

  @CreationTimestamp private LocalDateTime createdDate;
  private LocalDateTime expiryDate;

  @Enumerated(EnumType.STRING)
  private VerificationType verificationType;

  @Column(length = 20)
  @Email private String email;

  @Column(length = 30)
  private String token;

  public VerificationToken(String token, String email, VerificationType verificationType) {
    this.token = token;
    this.email = email;
    this.verificationType = verificationType;
  }

  @PrePersist
  public void setExpiry() {
    expiryDate = LocalDateTime.now().plusDays(1);
  }
}

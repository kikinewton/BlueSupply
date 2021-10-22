package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.CommentListener;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(CommentListener.class)
public class PettyCashComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String description;

    Boolean read;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    int pettyCashId;

    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;

}

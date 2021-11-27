package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.FloatCommentListener;
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
@EntityListeners(FloatCommentListener.class)
public class FloatComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String description;

    Boolean read;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "float")
    @JsonIgnore
    Floats floats;

    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;

}

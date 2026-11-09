package hu.bme.backend.entities;

import hu.bme.backend.entities.common.Base;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "shopping_items")
public class ShoppingItem extends Base {

    @NotBlank
    @Column(nullable = false, name = "product_name")
    private String productName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category", nullable = false)
    private Category category;

    @Column(name = "quantity")
    @Positive
    private BigDecimal qty;

    @Column(name = "unit")
    private String unit;

    @Column(name = "purchased", nullable = false)
    private boolean purchased = false;

    @Column(name = "comment", length = 250)
    @Size(max = 250)
    private String comment;

    //https://www.baeldung.com/hibernate-creationtimestamp-updatetimestamp
    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;
}



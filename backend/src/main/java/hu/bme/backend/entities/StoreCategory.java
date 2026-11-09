package hu.bme.backend.entities;

import hu.bme.backend.entities.common.Base;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "store_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"store_id", "category_id"}
                )
        }
)
public class StoreCategory extends Base {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Integer position;
}

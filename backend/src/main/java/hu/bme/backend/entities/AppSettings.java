package hu.bme.backend.entities;

import hu.bme.backend.entities.common.Base;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "app_settings")
public class AppSettings extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_store")
    private Store selectedStore;
}

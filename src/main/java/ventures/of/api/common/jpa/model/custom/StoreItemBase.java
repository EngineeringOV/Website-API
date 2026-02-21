package ventures.of.api.common.jpa.model.custom;

import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.List;

@Entity
@Data
public class StoreItemBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`uuid`")
    private String uuid;

    public String namePackage;
    public String subText;
    public String classes;
    public int fullPrice;
    public String priceUnits;
    public String imageUrl;
    public List<Integer> itemIdList;
}

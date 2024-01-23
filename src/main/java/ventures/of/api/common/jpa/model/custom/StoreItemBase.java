package ventures.of.api.common.jpa.model.custom;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
@Data
public class StoreItemBase {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
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

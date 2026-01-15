package capstone.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SellerReviewSummaryDto {
    private Double avgAll;
    private Double avg3m;
    private int cntAll;
    private int cnt3m;
}

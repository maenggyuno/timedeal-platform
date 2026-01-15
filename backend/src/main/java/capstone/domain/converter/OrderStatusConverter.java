//8월 11일 신규 추가 균오
package capstone.domain.converter;

import capstone.domain.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : OrderStatus.fromCode(dbData);
    }
}

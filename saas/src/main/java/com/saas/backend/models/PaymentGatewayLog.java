package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name="payment_gateway_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentGatewayLog extends BaseEntity {
    @Enumerated(EnumType.STRING) private RelatedEntityType relatedEntityType;
    private UUID relatedEntityId;
    @Enumerated(EnumType.STRING) private GatewayProvider gatewayProvider;
    @Enumerated(EnumType.STRING) private GatewayDirection direction;
    @Column(columnDefinition="jsonb") private String rawPayload;
    private Integer httpStatus;
    private Boolean signatureVerified;
}
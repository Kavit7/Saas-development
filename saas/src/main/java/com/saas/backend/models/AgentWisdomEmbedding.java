package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="agent_wisdom_embeddings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentWisdomEmbedding extends BaseEntity {
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="wisdom_id", nullable=false, unique=true)
    private AgentWisdom wisdom;
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition="vector(1536)", nullable=false)
    private float[] embedding;
    private String model;
}
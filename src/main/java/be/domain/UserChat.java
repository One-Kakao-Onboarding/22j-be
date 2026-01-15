package be.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChat extends AuditingCreation implements EntityId<Long> {

    @Id
    @GeneratedValue
    private Long id;

}

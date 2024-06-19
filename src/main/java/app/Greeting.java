package app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder @With
public class Greeting {
    String greeting;
    String target;
}

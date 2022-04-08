package fun.xuning.rpc.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloObject implements Serializable {
    private Integer id;
    private String message;

}
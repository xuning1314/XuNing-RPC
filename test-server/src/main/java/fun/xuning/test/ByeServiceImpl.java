package fun.xuning.test;

import fun.xuning.rpc.annotation.Service;
import fun.xuning.rpc.api.ByeService;

@org.springframework.stereotype.Service("ByeServiceImpl")
@Service
public class ByeServiceImpl implements ByeService {
    @Override
    public String bye(String name) {
        return name;
    }
}

package roi.ding.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import roi.ding.bean.MyResourceBean;

import java.util.List;

/**
 */
@Component
@Mapper
public interface ResourceMapper {
    /**
     * 从数据库中查询所有资源
     * @return
     */
    @Select("select * from resource ")
    List<MyResourceBean> selectAllResource();
}

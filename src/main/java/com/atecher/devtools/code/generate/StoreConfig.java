package com.atecher.devtools.code.generate;

import lombok.Builder;
import lombok.Getter;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/6/25 下午1:41
 */
@Getter
@Builder
//@AllArgsConstructor
public class StoreConfig{
    private String database;

    private String storePath;

    private String base_package;

    private String bean_path;

    private String mapper_path;

    private String service_path;

    private String i_service_path;

    private String xml_path;

    private String bean_package;

    private String mapper_package;

    private String service_package;

    private String i_service_package;

    public StoreConfig(){
        this.bean_path = storePath+"/entity_bean/"+database;

        this.mapper_path = storePath+"/entity_mapper_dao/"+database;

        this.service_path = storePath+"/entity_service/"+database;

        this.i_service_path =storePath+ "/entity_service/"+database;

        this.xml_path = storePath+"/entity_mapper/"+database;

        this.bean_package = base_package+".domain";

        this.mapper_package = base_package+".dao";

        this.service_package = base_package+".service";

        this.i_service_package = base_package+".rpc";
    }

    public StoreConfig(String database, String storePath, String base_package, String bean_path, String mapper_path, String service_path, String i_service_path, String xml_path, String bean_package, String mapper_package, String service_package, String i_service_package) {
        this.database = database;
        this.storePath = storePath;
        this.base_package = base_package;
        this.bean_path = bean_path;
        this.mapper_path = mapper_path;
        this.service_path = service_path;
        this.i_service_path = i_service_path;
        this.xml_path = xml_path;
        this.bean_package = bean_package;
        this.mapper_package = mapper_package;
        this.service_package = service_package;
        this.i_service_package = i_service_package;
        this.bean_path = storePath+"/entity_bean/"+database;

        this.mapper_path = storePath+"/entity_mapper_dao/"+database;

        this.service_path = storePath+"/entity_service/"+database;

        this.i_service_path =storePath+ "/entity_service/"+database;

        this.xml_path = storePath+"~/cg/entity_mapper/"+database;

        this.bean_package = base_package+".domain";

        this.mapper_package = base_package+".dao";

        this.service_package = base_package+".service";

        this.i_service_package = base_package+".rpc";
    }
}
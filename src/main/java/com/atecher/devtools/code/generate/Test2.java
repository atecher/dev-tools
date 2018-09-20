package com.atecher.devtools.code.generate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/7/28 下午4:06
 */
public class Test2 {
    public static void main(String[] args) throws IOException {
//        String sqlTem = FileUtils.readFileToString(new File("/Users/mark/mryt/gms_order_status_record.sql"), Charset.forName("utf-8"));


//        int databases = 10;
//        int tables = 256;
//
//        File sql = new File("/Users/mark/mryt/gms_order_status_record_sharding.sql");
//        for (int j = 0; j < tables; j++) {
//            FileUtils.write(sql, sqlTem.replaceAll("gms_order_status_record", "gms_order_status_record_" + j) + "\n\n", "utf-8", true);
//        }
        List<String> files=new ArrayList<>();
        List<String> orderDetails=FileUtils.readLines(new File("/Users/mark/process.txt"),Charset.forName("UTF-8"));
        List<String> shopShelf=FileUtils.readLines(new File("/Users/mark/success.txt"),Charset.forName("UTF-8"));
        Set<String> collect = shopShelf.stream().map(s -> {
            JSONObject jsonObject = JSON.parseObject(s);
            return jsonObject.getString("orderId") + jsonObject.getString("goodsCode");
        }).collect(Collectors.toSet());
        for (String orderDetail : orderDetails) {
            JSONObject jsonObject = JSONObject.parseObject(orderDetail);
           String  key=jsonObject.getString("orderId")+jsonObject.getString("goodsCode");
           if(!collect.contains(key)){
               boolean add = files.add(orderDetail);
           }
        }

        FileUtils.writeLines(new File("/Users/mark/fail.txt"),files);


    }

    private static Set<String> getArray(String shopCode,String shelfCode,HttpHeaders headers,RestTemplate restTemplate){

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("access_token","bDZFajNjSERvNFBLWktQSUZZeTRZWE5uMFBzeU5DeTBSTU16U2pkSWYvZ3FXUjl2UjhSQXd1SmJSK3RFSlczSg==");
        jsonObj.put("shopCode",shopCode);
        jsonObj.put("shelfCode",shelfCode);

        HttpEntity<String> formEntity = new HttpEntity<String>(jsonObj.toString(), headers);

        String result = restTemplate.postForObject("https://grampus-api.imrfresh.com/api/goods/getShelfGoodsListV4", formEntity, String.class);
        JSONObject jsonObject = JSON.parseObject(result);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("product_info_layer");
        Set<String> goodsSet=new HashSet<>();
        for(int i=0;i<jsonArray.size();i++){
            JSONArray dataList = jsonArray.getJSONObject(i).getJSONArray("products");
            for(int j=0;j<dataList.size();j++){
                String goods_no = dataList.getJSONObject(j).getString("goods_no");
                goodsSet.add(goods_no);
            }
        }

        return goodsSet;
    }



}

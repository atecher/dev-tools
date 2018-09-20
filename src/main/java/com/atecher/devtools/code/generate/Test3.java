package com.atecher.devtools.code.generate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
public class Test3 {
    public static void main(String[] args) throws IOException {

        List<String> files=new ArrayList<>();
        List<String> orderDetails=FileUtils.readLines(new File("/Users/mark/fail.txt"),Charset.forName("UTF-8"));
        List<String> shopExists=FileUtils.readLines(new File("/Users/mark/shop_exists.txt"),Charset.forName("UTF-8"));
        Set<String> exists = shopExists.stream().collect(Collectors.toSet());
        List<String> shopCodeList = orderDetails.stream().map(s ->
                JSON.parseObject(s,OrderDetail.class).getShopCode()
        ).filter(s->!exists.contains(s)).collect(Collectors.toSet()).stream().collect(Collectors.toList());


        FileUtils.writeLines(new File("/Users/mark/shop_not_exists.txt"),shopCodeList);


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


    private static void test() throws IOException {
        List<String> files=new ArrayList<>();
        List<String> orderDetails=FileUtils.readLines(new File("/Users/mark/fail.txt"),Charset.forName("UTF-8"));
        List<String> md2=FileUtils.readLines(new File("/Users/mark/md-2"),Charset.forName("UTF-8"));

        List<OrderDetail> shopCodeList = orderDetails.stream().map(s ->
                JSON.parseObject(s,OrderDetail.class)
        ).collect(Collectors.toList());
        Map<String,String> map=new HashMap<>();
        md2.forEach(s->{
            String[] split = s.split("===");
            map.put(split[0]+split[2],split[1]);
        });

        shopCodeList.forEach(s->{
            String key=s.getShopCode()+s.getGoodsCode();
            if(map.containsKey(key)){
                s.setShelfCode(map.get(key));
                files.add(JSON.toJSONString(s));
            }
        });
        FileUtils.writeLines(new File("/Users/mark/success2.txt"),files);

    }



}

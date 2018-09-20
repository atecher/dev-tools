package com.atecher.devtools.code.generate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
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
public class Test {
    public static void main(String[] args) throws IOException {
//        String sqlTem = FileUtils.readFileToString(new File("/Users/mark/mryt/gms_order_status_record.sql"), Charset.forName("utf-8"));


//        int databases = 10;
//        int tables = 256;
//
//        File sql = new File("/Users/mark/mryt/gms_order_status_record_sharding.sql");
//        for (int j = 0; j < tables; j++) {
//            FileUtils.write(sql, sqlTem.replaceAll("gms_order_status_record", "gms_order_status_record_" + j) + "\n\n", "utf-8", true);
//        }
        List<String> orderDetails=FileUtils.readLines(new File("/Users/mark/process.txt"),Charset.forName("UTF-8"));
        List<String> shopShelf=FileUtils.readLines(new File("/Users/mark/shop_shelf.txt"),Charset.forName("UTF-8"));
        Map<String, List<OrderDetail>> shopCodeList = orderDetails.stream().map(s ->
            JSON.parseObject(s,OrderDetail.class)
        ).collect(Collectors.groupingBy(OrderDetail::getShopCode));

        Map<String, List<ShopShelf>> shopShelfList = shopShelf.stream().map(s ->
                JSON.parseObject(s,ShopShelf.class)
        ).collect(Collectors.groupingBy(ShopShelf::getShopCode));

        RestTemplate restTemplate=new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        List<String> success=new ArrayList<>();
        shopCodeList.keySet().forEach(shopCode->{
            List<ShopShelf> shopShelves = shopShelfList.get(shopCode);
            List<OrderDetail> orderDetail = shopCodeList.get(shopCode);
            if(shopShelves!=null){
                shopShelves.forEach(s -> {
                    if(s!=null){
                        try{
                            Set<String> goods=getArray(s.getShopCode(), s.getShelfCode(), headers, restTemplate);
                            orderDetail.forEach(ss->{
                                if(goods.contains(ss.getGoodsCode())){
                                    ss.setShelfCode(s.getShelfCode());
                                    System.out.println(JSON.toJSONString(ss));
                                    try {
                                        FileUtils.writeStringToFile(new File("/Users/mark/success.txt"),JSON.toJSONString(ss)+"\r\n",true);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    success.add(JSON.toJSONString(ss));
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                });
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });




//        System.out.println(String.join("','",shopCodeList));




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

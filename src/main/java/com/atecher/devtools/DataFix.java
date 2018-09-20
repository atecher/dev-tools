package com.atecher.devtools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mryx.grampus.common.Constant;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/8/7 下午11:16
 */
public class DataFix {

    public static void fix(JSONObject order, List<JSONObject> detail, BigDecimal couponPrice) throws IOException {
//        if(couponPrice==null){
//            return;
//        }

        BigDecimal totalCouponPrice = new BigDecimal(0);
        BigDecimal curTotalPayPrice = new BigDecimal(0);
        BigDecimal payPrice = order.getBigDecimal("payPrice");
        Integer   couponDetailSize=0;
        Map<String, BigDecimal> priceMap = new HashMap<>();
        for (int i = 0; i < detail.size(); i++) {
                   JSONObject item = detail.get(i);
              if (item.getBoolean("useCoupon")) {
                  couponDetailSize++;
                  BigDecimal discountPrice = item.getBigDecimal("discountPrice");
                  BigDecimal productPayPrice = discountPrice.multiply(item.getBigDecimal("productNum"));
                  if(item.getBoolean("useGoodCoupon")){
                      productPayPrice=productPayPrice.subtract(discountPrice);
                  }
                  String productId = item.getString("productId");
                  priceMap.put(productId, productPayPrice);
                  totalCouponPrice=totalCouponPrice.add(productPayPrice);
              }
        }


        if (totalCouponPrice != null && totalCouponPrice.compareTo(BigDecimal.ZERO) == 1) {
            for (int i = 0; i < detail.size(); i++) {
                JSONObject item = detail.get(i);
                BigDecimal discountPrice = item.getBigDecimal("discountPrice");
                String productId = item.getString("productId");
                BigDecimal productPayPrice = discountPrice.multiply(item.getBigDecimal("productNum"));
                if(item.getBoolean("useGoodCoupon")){
                    productPayPrice=productPayPrice.subtract(discountPrice);
                }
                // 单品总价占优惠券金额的多少
                if (item.getBoolean("useCoupon")) {
                    BigDecimal ratioPrice = productPayPrice.multiply(couponPrice).divide(totalCouponPrice, 2, BigDecimal.ROUND_DOWN);
                    productPayPrice = productPayPrice.subtract(ratioPrice);


                    productPayPrice = productPayPrice.compareTo(Constant.bd_0) < 0 ? Constant.bd_0 : productPayPrice;
                }
                curTotalPayPrice = curTotalPayPrice.add(productPayPrice);
                priceMap.put(productId, productPayPrice);
            }


        }

        if (payPrice.compareTo(curTotalPayPrice) < 0) {
            BigDecimal surplus = curTotalPayPrice.subtract(payPrice);
            int surplusFen = surplus.multiply(Constant.bd_100).intValue();
            int quotient = (surplusFen / couponDetailSize);
            int reminder = surplusFen % couponDetailSize;
            int index = 0;
            for (int i = 0; i < detail.size(); i++) {
                JSONObject item = detail.get(i);
                String productId = item.getString("productId");
                BigDecimal productPayPrice = priceMap.get(productId);
                if (item.getBoolean("useCoupon")) {
                    productPayPrice = productPayPrice.subtract(new BigDecimal(0.01).multiply(new BigDecimal(quotient)));
                    index++;
                    if (index <= reminder) {
                        productPayPrice = productPayPrice.subtract(new BigDecimal(0.01));
                    }
                    if (productPayPrice.compareTo(BigDecimal.ZERO) < 0) {
                        productPayPrice = BigDecimal.ZERO;
                    }
                }
                priceMap.put(productId, productPayPrice);



            }
        }
        BigDecimal totalPayPrice=new BigDecimal(0);
        for (int i = 0; i < detail.size(); i++) {
            JSONObject item = detail.get(i);
            String productId = item.getString("productId");
            totalPayPrice=totalPayPrice.add(priceMap.get(productId));


        }
        System.out.println(totalPayPrice.doubleValue()==order.getBigDecimal("payPrice").doubleValue());
//        System.out.println(order.getBigDecimal("payPrice").doubleValue());

        for (int i = 0; i < detail.size(); i++) {
            JSONObject item = detail.get(i);
            String productId = item.getString("productId");
            BigDecimal productPayPrice = priceMap.get(productId);

            String sql = "update gms_order_detail set product_pay_price=%s where order_id='%s' and product_id='%s';";
            String format = String.format(sql, productPayPrice.doubleValue(), item.getString("orderId"), item.getString("productId"));
            FileUtils.writeStringToFile(new File("/Users/mark/update_order.sql"),format+"\r\n",true);


        }
    }


    public static List<JSONObject> orderList() throws IOException {
        List<String> strings = FileUtils.readLines(new File("/Users/mark/order.txt"), "utf-8");
        List<JSONObject> collect = strings.stream().map(s -> {
            String[] split = s.split("====");
            JSONObject item = new JSONObject();
            item.put("orderId", split[0]);
            item.put("payPrice", new BigDecimal(split[1]));
            item.put("totalPrice", new BigDecimal(split[2]));
            item.put("discountPrice", new BigDecimal(split[3]));
            item.put("couponId", split[4]);
//            System.out.println(item.toJSONString());
            return item;
        }).collect(Collectors.toList());
        return collect;
    }

    public static Map<String, List<JSONObject>> detailListMap() throws IOException {
        List<String> strings = FileUtils.readLines(new File("/Users/mark/order_detail.txt"), "utf-8");
        List<JSONObject> collect = strings.stream().map(s -> {
            String[] split = s.split("====");
            JSONObject item = new JSONObject();
            item.put("orderId", split[0]);
            item.put("productId", split[1]);
            Integer productNum = Integer.valueOf(split[2]);
            item.put("productNum", productNum);
            item.put("productPrice", new BigDecimal(split[3]));
            BigDecimal discountPrice = new BigDecimal(split[4]);
            item.put("discountPrice", discountPrice);
            BigDecimal productPayPrice = new BigDecimal(split[5]);
            item.put("productPayPrice", productPayPrice);
            item.put("useCoupon", true);
            item.put("useGoodCoupon",split[1].equals("blg-t1"));


//            System.out.println(item.toJSONString());
            return item;
        }).collect(Collectors.toList());
        Map<String, List<JSONObject>> detailListMap = new HashMap<>();
        for (JSONObject item : collect) {
            String orderId = item.getString("orderId");
            List<JSONObject> jsonObjects = detailListMap.get(orderId);
            if (jsonObjects != null) {
                jsonObjects.add(item);
            } else {
                jsonObjects = new ArrayList<>();
                jsonObjects.add(item);

            }
            detailListMap.put(orderId, jsonObjects);
        }

        return detailListMap;

    }


    public static Map<String, BigDecimal> couponMap() throws IOException {
//        List<String> strings = FileUtils.readLines(new File("/Users/mark/coupon.txt"), "utf-8");
//        List<JSONObject> collect = strings.stream().map(s -> {
//            JSONObject item = JSON.parseObject(s);
//            return item;
//        }).collect(Collectors.toList());
        Map<String, BigDecimal> detailListMap = new HashMap<>();
//        for (JSONObject item : collect) {
//            String couponUserId = item.getString("couponUserId");
//            BigDecimal val = item.getBigDecimal("val");
//
//            detailListMap.put(couponUserId, val);
//        }
        detailListMap.put("454164",new BigDecimal("5.53"));
//        System.out.println(JSON.toJSONString(detailListMap));
        return detailListMap;

    }

    public static void main(String[] args) throws IOException {
        List<JSONObject> jsonObjects = orderList();
        Map<String, List<JSONObject>> stringListMap = detailListMap();
        Map<String, BigDecimal> couponMap = couponMap();
        for (JSONObject order : jsonObjects) {
            fix(order,stringListMap.get(order.getString("orderId")),couponMap.get(order.getString("couponId")));
        }



    }
}





package org.opengis.yang.geotools.shape;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;
import org.opengis.yang.geotools.filter.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * shape文件的读取
 */
public class ShapeFileReader {

    private FeatureSource featureSource;

    //数据字段以及类型
    private Map<String,String> atttibuteTypeMap;

    @Before
    public void readShapeFile() throws IOException {
        final String filePath = "D:\\POIshan3xi_point.shp";
        Map<String,Object> param = new HashMap<>();
        // 具体可参见ShapefileDataStoreFactory中关于shape数据源的参数
        param.put(ShapefileDataStoreFactory.URLP.getName(),new File(filePath).toURI().toURL());
        param.put(ShapefileDataStoreFactory.DBFCHARSET.getName(),"GBK");
        DataStore dataStore = DataStoreFinder.getDataStore(param);
        //获取到featuresource
        this.featureSource = dataStore.getFeatureSource(dataStore.getNames().get(0));
        this .atttibuteTypeMap = featureSource
                                .getSchema()
                                .getDescriptors()
                                .parallelStream()
                                .collect(
                                HashMap::new,
                                (map,descriptor)->map.put(descriptor.getName().getLocalPart(),descriptor.getType().getBinding().getName()),
                                HashMap::putAll
        );
        int featureCount = featureSource.getFeatures().size();
        System.out.println("原要素总数：" + featureCount);
    }

    /**
     * 测试sql中的like
     */
    @Test
    public void testFilterLike() throws CQLException, IOException {
        //like
        Filter likeFilter = FilterBuilder.createCommonFilter("NAME like '%学校'");
        Filter likeFilter1 = FilterBuilder.createLikeFiter("NAME","*学校");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(likeFilter);
        System.out.println( "过滤后的要素总数：" + featureCollection.size());
    }

    /**
     * 测试sql中的=
     * @throws CQLException
     * @throws IOException
     */
    @Test
    public void testFilterEqual() throws CQLException, IOException {
        //= 过滤
        Filter equalFilter = FilterBuilder.createCommonFilter("NAME = '西安博纳影视培训学校'");
        Filter equalFilter1 = FilterBuilder.creatEqualFilter("NAME","西安博纳影视培训学校");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(equalFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }


    /**
     * 测试sql中的>=
     * @throws CQLException
     * @throws IOException
     */
    @Test
    public void testFilterEqualOrGreater() throws CQLException, IOException {
        // >= 过滤
        Filter greaterFilter = FilterBuilder.createCommonFilter("KIND >= '160100'");
        Filter greaterFilter1 = FilterBuilder.createGreaterOrEqualFilter("KIND",160100,true);
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(greaterFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }

    /**
     * 使用逻辑or谓词实现sql中的in操作
     * @throws CQLException
     * @throws IOException
     */
    @Test
    public void testFilterIN() throws IOException {
        //in 过滤
        Filter inFilter = FilterBuilder.createInFilter("KIND",new String[]{"110304","110303","130201"});
        //新版本的geotools 直接通过cql实现in不生效
//        Filter inFilter1 = FilterBuilder.createCommonFilter("KIND IN ('110304','110303','130201')");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(inFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }


    /**
     *
     */
    @Test
    public void testSpatialBbox() throws CQLException, IOException {
        //某个bbox内的要素cql
        Filter bboxFilter = FilterBuilder.createCommonFilter("BBOX(the_geom, 116.0, 39.0, 117.0, 40.0)");
        //ECQL 语法
        Filter bboxFilter1 = FilterBuilder.createBboxFilter("the_geom", 106.5, 35.0, 107.5, 36.0,"EPSG:4326");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(bboxFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }


    @Test
    public void testSpatialBuffer() throws CQLException, IOException, ParseException {
        //实现给定一个点和半径，查找落在 某圆内的要素
        Filter distanceFilter = FilterBuilder.createCommonFilter("DWITHIN(the_geom, POINT(108.0 35.5), 1000, 'meters')");
        //实现给定一个点和距离，过滤到这个点的距离小于指定距离的所有要素
        Filter distanceFilter1 = FilterBuilder.createSpatialDistanceFilter("the_geom", "POINT(108.0 35.5)",1000,"meters");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(distanceFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }


    @Test
    public void testSpatialDistance() throws CQLException, IOException, ParseException {
        //空间距离大于某个指定值    如下实现大于指定点100km以外的要素
        Filter beyondfilter = FilterBuilder.createCommonFilter("BEYOND(geom, POINT(108 35), 100, kilometers)");
        //空间相交
        Filter beyondfilter1 = FilterBuilder.createDistanceFilter("the_geom","POINT(108 35)",100,"kilometers");
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(beyondfilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }

    @Test
    public void testSpatialIntersect() throws CQLException, IOException {
        // 1. 使用 WKT 格式定义参考几何
        String geomWKT = "POLYGON((106 36.5, 107 36.5, 107 37.5, 106 37.5, 106 36.5))";
        String ecql = "INTERSECTS(the_geom, " + geomWKT + ")";
        Filter intersectFilter = ECQL.toFilter(ecql);
        Filter intersectFilter1 = FilterBuilder.createIntersect("the_geom",geomWKT);
        //过滤查询
        FeatureCollection featureCollection = featureSource.getFeatures(intersectFilter);
        System.out.println("过滤后的要素总数：" + featureCollection.size());
    }
}

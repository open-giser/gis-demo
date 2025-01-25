package org.opengis.yang.geotools.shape;

import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

public class GisShapeWrite {

    private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

    @Test
    public void testCrs() throws FactoryException, IOException {
        String newShapeName = "D:\\test\\shp\\test.shp";
        ShapefileDataStore shapefileDataStore = new ShapefileDataStore(Paths.get(newShapeName).toUri().toURL());
    }

    @Test
    public void writeShapeFromDataStore() throws IOException, FactoryException {
        String newShapeName = "D:\\test\\shp\\test.shp";
        //创建一个shape文件的对象
        ShapefileDataStore dataStore = null;
        Transaction transaction = null;
        try{
            dataStore = new ShapefileDataStore(Paths.get(newShapeName).toUri().toURL());
            //创建featuretype
            SimpleFeatureType simpleFeatureType = createSimpleFeatureType();
            dataStore.createSchema(simpleFeatureType);
            dataStore.setCharset(Charset.forName("UTF-8"));
            //构建simplefeature的存储对象
            SimpleFeatureStore simpleFeatureStore = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            //构建一个操作事务
            transaction = new DefaultTransaction("handle");
            //设置事务
            simpleFeatureStore.setTransaction(transaction);
            //添加要素信息
            simpleFeatureStore.addFeatures(createSimpleFeatures(simpleFeatureType));
            transaction.commit();
        } catch (Exception e) {
            if (dataStore != null) {
                dataStore.dispose();
            }
            if (transaction != null) {
                transaction.rollback();
                transaction.close();
            }
            throw new RuntimeException(e);
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
            if (transaction != null) {
                transaction.close();
            }
        }
    }

    /**
     *
     * @return
     * @throws FactoryException
     */
    private FeatureCollection createSimpleFeatures(SimpleFeatureType simpleFeatureType) throws FactoryException {
        SimpleFeature simpleFeature = createSimpleFeature(simpleFeatureType);
        //构建属性要素集合，当然simpleFeature可以创建一个集合
        return new ListFeatureCollection(simpleFeatureType,simpleFeature);
    }

    /**
     * 构建FeatureType
     * @return  返回FeatureType
     * @throws FactoryException
     */
    private SimpleFeatureType createSimpleFeatureType() throws FactoryException {
        SimpleFeatureTypeBuilder simpleFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
        //设置属性字段
        simpleFeatureTypeBuilder.add("id", Integer.class);
        //设置name字段的长度
        simpleFeatureTypeBuilder.length(50).add("name", String.class);
        //设置code字段不能为空
        simpleFeatureTypeBuilder.nillable(false).add("code",String.class);
        //设置几何字段
        simpleFeatureTypeBuilder.add("the_geom", LineString.class,4326);
        simpleFeatureTypeBuilder.setName("test");
        return simpleFeatureTypeBuilder.buildFeatureType();
    }

    private SimpleFeature createSimpleFeature(SimpleFeatureType simpleFeatureType) throws FactoryException {
        /**
         * 创建SimpleFeature的方法很多，最常见的就是使用SimplefeatureBuilder
         * 也可以使用geotools的DataUtilities.createFeature()方法来实现
         */
        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(simpleFeatureType);
        simpleFeatureBuilder.set("id", 1);
        simpleFeatureBuilder.set("name", "高速路");
        simpleFeatureBuilder.set("code","100116");
        simpleFeatureBuilder.set("the_geom",geometryFactory.createLineString(new Coordinate[]{new Coordinate(108, 34), new Coordinate(108.5, 34.5)}));
        return simpleFeatureBuilder.buildFeature("fid");
    }
}

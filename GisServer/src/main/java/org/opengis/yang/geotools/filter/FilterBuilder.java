package org.opengis.yang.geotools.filter;

import org.geotools.api.data.Query;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.util.factory.GeoTools;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 根据Geotools中的
 */
public class FilterBuilder {

    private static final FilterFactory FILTER_FACTORY = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());

    private static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    private static final WKTReader2 WKT_READER_2 = new WKTReader2(GEOMETRY_FACTORY);


    /**
     *  构造通用过滤表达式，
     *  比如 1、属性过滤条件Filter filter = CQL.toFilter("attName >= 5");
     *  2、空间查询条件Filter pointInPolygon = CQL.toFilter("CONTAINS(THE_GEOM, POINT(1 2))");
     *  3、Filter clickedOn = CQL.toFilter("BBOX(ATTR1, 151.12, 151.14, -33.5, -33.51)";
     * @param cqlString  cql字符串表达式
     * @throws CQLException
     */
    public static Filter createCommonFilter(String cqlString) throws CQLException {
        return CQL.toFilter(cqlString);
    }


    public static Filter createDistanceFilter(String geomName,String pointWkt,double distance,String units) throws ParseException {
        return FILTER_FACTORY.beyond(geomName,WKT_READER_2.read(pointWkt),distance,units);
    }

    /**
     *  构造大于过滤器
     * @param attrName  属性名称
     * @param literValue 比较值
     * @param matchCase 是否或略大小写
     * @return 返回filter对象
     */
    public static Filter createGreaterFilter(String attrName,int literValue,boolean matchCase) {
        return FILTER_FACTORY.greater(FILTER_FACTORY.property(attrName),FILTER_FACTORY.literal(literValue),matchCase);
    }

    /**
     *  构造大于过滤器
     * @param attrName  属性名称
     * @param literValue 比较值
     * @param matchCase 是否或略大小写
     * @return 返回filter对象
     */
    public static Filter createGreaterOrEqualFilter(String attrName,int literValue,boolean matchCase) {
        return FILTER_FACTORY.greaterOrEqual(FILTER_FACTORY.property(attrName),FILTER_FACTORY.literal(literValue),matchCase);
    }

    /**
     *  构造小于过滤器
     * @param attrName  属性名称
     * @param literValue 比较值
     * @return 返回filter对象
     */
    public static Filter createLowerFilter(String attrName,int literValue) {
        return FILTER_FACTORY.less(FILTER_FACTORY.property(attrName),FILTER_FACTORY.literal(literValue));
    }


    /**
     *  构造相等过滤器
     * @param attrName  属性名称
     * @param literValue 比较值
     * @return 返回filter对象
     */
    public static Filter creatEqualFilter(String attrName,String literValue) {
        return FILTER_FACTORY.equal(FILTER_FACTORY.property(attrName),FILTER_FACTORY.literal(literValue),true);
    }

    /**
     *  构造相似过滤器
     * @param attrName  属性名称
     * @param pattern 比较值
     * @return 返回filter对象
     */
    public static Filter createLikeFiter(String attrName,String pattern) {
        return FILTER_FACTORY.like(FILTER_FACTORY.property(attrName),pattern);
    }

    /**
     *  构造空间包含过滤器
     * @param geomName geometry的字段名称
     * @param geometry 需要比较的geometry对象
     * @return 返回geometry包含的要素独享
     * @throws CQLException
     */
    public static Filter createSpatialContainsFilter(String geomName, Geometry geometry) {
        return FILTER_FACTORY.contains(geomName,geometry);
    }

    /**
     * 构造以某个点为中心，distance为半径的缓冲区
     * 过滤落在该缓冲区内的要素数
     * @param geomName
     * @param geomWkt  点数据
     * @param distance 缓冲距离，
     * @param untis 距离单位
     * @return
     */
    public static Filter createSpatialDistanceFilter(String geomName, String geomWkt, double distance,String untis) throws ParseException {
        return FILTER_FACTORY.dwithin(geomName,WKT_READER_2.read(geomWkt),distance,untis);
    }


    /**
     * 实现数据的分页查询
     * @param cqlString 过滤查询条件,为空时表示查询所有
     * @param startIndex 其实索引
     * @param pageNum 当前页的所有数量
     * @return 返回query对象
     * @throws CQLException
     */
    public static Query createPageQuery(String cqlString, int startIndex, int pageNum) throws CQLException {
        Filter filter = createCommonFilter(cqlString);
        Query query =  new Query();
        query.setFilter(filter);
        query.setStartIndex(startIndex);
        query.setMaxFeatures(pageNum);
        return query;
    }

    /**
     * cqlfilter构造表达式
     * @return
     */
    public static void createExpression() throws CQLException {
        //构造缓冲
        CQL.toExpression("buffer(THE_GEOM)");
        //使用strConcat函数构造
        CQL.toExpression("strConcat(CITY_NAME, POPULATION)");
        //构造距离
        CQL.toExpression("distance(THE_GEOM, POINT(151.14,-33.51))");
    }

    /**
     * 使用or逻辑运算符创建in的表达式
     * @param propertyName
     * @param matchValues
     * @return
     */
    public static Filter createInFilter(String propertyName, String[] matchValues) {
        List<Filter> filters = new ArrayList<>();
        Arrays.stream(matchValues).forEach(matchValue -> {
            Filter filter = FILTER_FACTORY.equal(FILTER_FACTORY.property(propertyName), FILTER_FACTORY.literal(matchValue));
            filters.add(filter);
        });
        return FILTER_FACTORY.or(filters);
    }

    /**
     * 获取指定范围内的要素
     * @param theGeom
     * @param x1  最小x 坐标
     * @param y1  最小y
     * @param x2  最大x 坐标
     * @param y2  最大y 坐标
     * @param srs 对应范围的投影坐标
     * @return
     */
    public static Filter createBboxFilter(String theGeom, double x1, double y1 , double x2, double y2,String srs) {
         return FILTER_FACTORY.bbox(theGeom, x1, y1, x2, y2,srs);
    }

    /**
     * 创建相交过滤对象
     * @param theGeom geom字段名称
     * @param geomWKT
     * @return
     * @throws ParseException
     */
    public static Filter createIntersect(String theGeom, String geomWKT) throws ParseException {
        return FILTER_FACTORY.intersects(theGeom,WKT_READER_2.read(geomWKT));
    }
}

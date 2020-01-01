# 总体计划： 完善数据访问Jdbc组件能力，提高生产力。

# JdbcX 1.2.0.RELEASE
# 一、数据源修改
去掉了DataConfig这个类，配置数据源时，只需在配置文件中引用数据库配置信息即可,前面的project.datasource.固定，secondary为引用数据源使用,支持多数据源。
```application.properties
project.datasource.secondary.url = jdbc:sqlserver://192.168.70.28;DatabaseName=WXAppZWMC
project.datasource.secondary.username = sa
project.datasource.secondary.password= abc.123
project.datasource.secondary.driver-class-name = com.microsoft.sqlserver.jdbc.SQLServerDriver
```
```java 
@DataRepository(name = "secondary")
public class StudengRepository extends BeanRepository<Student> {
}
```
# 二、引入xml
在对数据库操作时可以将sql写入xml文件中，目录为resources下的sqlmapper文件夹下, 以下为调用实例
```java
public class Test{
    @Autowired
    private StudengRepository studengRepository;
    
    public void tesst(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("classname", "2班");
        map.put("age",6);
        //studentMapper表示：xml的文件名；getStudent表示：文件中的方法名；map表示查询的参数；Student返回的实体
        //1.查询
        Student getStudent = studengRepository.QueryForObjectBySqlMap("studentMapper.getStudent", map, Student.class);  //单个查询
        List<Student> students = studengRepository.QueryBySqlMap("studentMapper.getStudent", map, Student.class);       //集合查询
        //2.对于单表的增删改 不建议用xml,可以使用原来的方式,如果是mysql的批量insert ，可以使用xml如下面xml的studentMapper.saveStudentList
        HashMap<String, List<Student>> mapList = new HashMap<>();
        List<Student> list=new ArrayList<>();
        mapList.put("userList",list);
        studengRepository.UpdateBySqlMap("studentMapper.saveStuList", mapList);
        
    }
}
```
```xml
<?xml version="1.0" encoding="UTF-8"?>
<sqls>
    <!--简单查询
        <![CDATA[  ]]> 表示:如果使用Velocity的模板语法需要用它包起来，如果不用if，foreach等语法 可以不用它
    -->
    <sqlElement key="studentMapper.getStudent">
        <![CDATA[
		select * from student where classname='$classname'
        #if( $age > 4 &&  $age <= 10 )
            and name='y'
        #else
            and name='s'
        #end
        ]]>
    </sqlElement>
    <!--添加-->
    <sqlElement key="studentMapper.saveStudent">
		insert into student (name,age,classname) value ("$name",$age,"$classname")
    </sqlElement>
    <!--批量添加
        最后生成的sql语句是：   insert into student (name) values ("张三"),("李四"),("王五") 
    -->
    <sqlElement key="studentMapper.saveStudentList">
        <![CDATA[
        insert into student (name) values
        #foreach ($user in $userList)
            ("$user.name")
            #if ($velocityCount < $userList.size())
                ,
            #else
            #end
        #end
        ]]>
    </sqlElement>

    <!--修改-->
    <sqlElement key="studentMapper.updateStudent">
        update student set name="$name",age=$age where id=$id
    </sqlElement>

    <!--表连接-->
    <sqlElement key="studentMapper.queryStuAndUserInfo">
        select id,name,age,u.UserId from student s left join UserInfo u on s.id=u.UserId where s.id=$id
    </sqlElement>
</sqls>
```
# 三、mysql支持分页
分页的调用方式与原来一样，底层判断数据源进行分页
```jdbcx分页调用
HashMap<String,Integer> params=new HashMap<>();
params.put("userid",40);
int pageIndex=2;
int pageSize=5;
//第五个参数为null即可,排序在第一个原始sql中添加
PageResult<Student> pageResult = studengRepository.getPageResult("select * from student where id < :userid ORDER BY id desc", params, pageIndex, pageSize, null, Student.class);
```

# JdbcX 1.1.0.RELEASE

# 总体计划： 完善数据访问JdbcX组件能力，提高生产力。
## 实现功能如下：
* 一.单表查询能力Where不需要SQL。
* 二.存储过程多返回值与实体映射 。
* 三.分页功能封装及实体映射。

## JDBCX API设计：
## 1.实体API设计
Java中无ExpressionTree，从而无法实现实现属性的动态解析，目前设计使用静态内部类常量形式，API设计如下：
```java
@Data
@Entity
//Table注解表示类型对应表结构 name 为 表名。
@Table(name = "UserInfo")
public class UserInfoEntity {

    /**
     * 表字段定义静态类
     */
    public static final class Columns {
        public static final String userid = "UserId";
        public static final String openid = "OpenId";
        public static final String unionid = "UnionId";
        public static final String nickName = "NickName";
        public static final String gender = "Gender";
    }

    //id表示这个字段为主键
    @Id
    //表示自增列
    @GeneratedValue
    //对应列名 name = 表中的列名
    @Column(name="UserId")
    private Long userId;

    @Column(name="OpenId")
    private String openId;

    @Column(name="UnionId")
    private String unionId;

    @Column(name="NickName")
    private String nickName;

    @Column(name="Gender")
    private String gender;
}
```
实体通过代码生成器生成而得。

## 2.BeanRepository (单表仓储类)
单表操作都使用抽象类扩展单表能力，目前已包含save,update,delete等，实现增删改等操作方法。
### 单表操作类声明
```java
@DataRepository(name = "wxappzwmc")
public class UserBeanRepository extends BeanRepository<UserInfoEntity>{  }
```
### 1.查询API设计
```java
//获取本仓库查询类
//QueryBuilder queryBuilder = userBeanRepository.createQuery();

QueryBuilder queryBuilder = new EntityQueryBuilder<>(UserInfoEntity.class);

queryBuilder.Where()
        .And(QueryCondition.LE(UserInfoEntity.Columns.userid,9))
        .And(QueryCondition.EQ(UserInfoEntity.Columns.nickName,"张磊"))
        .And(QueryCondition.LE(UserInfoEntity.Columns.createTime, QFunc.eq("getdate()")));
//查询本类对应实体
List<UserInfoEntity> users1 = userBeanRepository.queryForList(queryBuilder);

//查询任意表实体
List<UserInfoEntity> users = userBeanRepository.queryForList(queryBuilder,UserInfoEntity.class);

```

### 2.存储过程返回值API设计
```java
ProcQueryBuilder procQueryBuilder = new ProcQueryBuilder("Proc_JavaTest");
procQueryBuilder.AddInputParam("ModelId", 1825);
procQueryBuilder.AddInputParam("ModelName", "GL8");
procQueryBuilder.AddOutputParam("Count", 0);

XDataSet dataSet = queryForProcRepository.QueryForProc(procQueryBuilder);

Assert.assertEquals(dataSet.getTableCount(),3);

List<CsPhotoYcEntity> result0 = dataSet.getFirstResultForList(CsPhotoYcEntity.class);
Assert.assertTrue(result0.size() > 0);
List<CsPhotoYcEntity> result1 = dataSet.getResultForList(1, CsPhotoYcEntity.class);
Assert.assertTrue(result1.size() > 0);

Integer count = (Integer)dataSet.getOutputValue("Count");
Assert.assertTrue(count.equals(dataSet.getTableRowCount(0)));

```

### 3.分页API设计
```java
//根据SQL生产分页数据
HashMap<String,Object> params = new HashMap<>();
params.put("userid",50);

int count =  userBeanRepository.QueryForObject("select count(0) from (select * from UserInfo where UserId <50) query1111",new Object[]{},Integer.class);

int pageIndex = 1;
int pageSize = 10;

PageResult<UserInfoEntity> pageResult1 = userBeanRepository.getPageResult(
        "select * from UserInfo where UserId < :userid",params,pageIndex,pageSize,
                                        UserInfoEntity.Columns.userid,UserInfoEntity.class);

Assert.assertEquals(pageResult1.getPageIndex(),1);
Assert.assertEquals(pageResult1.getPageSize(),10);
Assert.assertEquals(pageResult1.getResultCount(),count);

PageResult<UserInfoEntity> pageResult2 = userBeanRepository.getPageResult(
        "select * from UserInfo where UserId < :userid",params,pageIndex + 1,pageSize,
        UserInfoEntity.Columns.userid,UserInfoEntity.class);

Assert.assertEquals(pageResult2.getPageIndex(),2);
Assert.assertEquals(pageResult2.getPageSize(),10);
Assert.assertEquals(pageResult2.getResultCount(),count);

```
# NativeRepository实现
NativeRepository 是一个jdbctemplate的抽象类，用于在编写查询类SQL时，由于JPA声明Repository接口的限制，不能在一个Repository内实现多个自定义实体返回。
NativeRepository 提供了Helper的方式，简化开发人员这方便的工作。

```java
@Repository
public class c1QueryNativeRepository  extends NativeRepository {
    //SQL参数化，获取userinfo表的数据，并返回实体集合
    public List<SerialEntity> getUsers(int id) throws Exception{
        String sql = "select * from UserInfo where UserId <= :id";
        HashMap map = new HashMap();
        map.put("id",id);
        List<SerialEntity> beans = super.Query(sql,map,SerialEntity.class);
        return beans;
    }
    //SQL参数化，返回单值。
    public Integer getCount(){
        String sql = "select count(*) from UserInfo";
        Object count = super.QueryForSingle(sql, new HashMap());
        return (Integer)count;
    }
    //执行存储过程，并返回结果集。
    public List<Map<String,Object>> GetCityInfoByProc(){
        String sql = "exec Proc_City_GetInfo";
        return super.Query(sql,new HashMap());
    }
    //多结果集也可以通过getProcedureData返回。

}
```


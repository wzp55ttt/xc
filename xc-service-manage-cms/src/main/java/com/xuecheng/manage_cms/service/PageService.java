package com.xuecheng.manage_cms.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.dao.CmsconfigRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * @author gakki
 */
@Service
public class PageService {

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private CmsconfigRepository cmsconfigRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

     //页面静态化
     public String getPageHtml(String pageId){
         //获取页面模型数据
         Map model = this.getModelByPageId(pageId);
         if(model == null){
             //获取页面模型数据为空
             ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
         }
         //获取页面模板
         String templateContent = getTemplateByPageId(pageId);
         if(StringUtils.isEmpty(templateContent)){
             //页面模板为空
             ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
         }
         //执行静态化
         String html = generateHtml(templateContent, model);
         if(StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
         }
         return html;
     }

     //页面静态化
     public String generateHtml(String template,Map model){
         try {
             //生成配置类
             Configuration configuration = new Configuration(Configuration.getVersion());
             //模板加载器
             StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
             stringTemplateLoader.putTemplate("template",template);
             //配置模板加载器
             configuration.setTemplateLoader(stringTemplateLoader);
             //获取模板
             Template template1 = configuration.getTemplate("template");
             String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
             return html;
         } catch (Exception e) {
            e.printStackTrace();
         }
         return null;
     }
     //获取页面模板
     public String getTemplateByPageId(String pageId){
         //查询页面信息
         Optional optional = this.cmsPageRepository.findById(pageId);
         if (!optional.isPresent()) {
             //页面不存在
             ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
         }
         CmsPage cmsPage = (CmsPage) optional.get();

         //页面模板
         String templateId = cmsPage.getTemplateId();

         if(StringUtils.isEmpty(templateId)){
             //页面模板为空
             ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
         }
         Optional<CmsTemplate> optiona = cmsTemplateRepository.findById(templateId);
         if(optional.isPresent()){
             CmsTemplate cmsTemplate = optiona.get();
             //模板文件id
             String templateFileId = cmsTemplate.getTemplateFileId();
             //取出模板文件内容
             //根据id查询
             GridFSFile id = this.gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
             //打开流对象
             GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(id.getObjectId());
             //创建resource
             GridFsResource gridFsResource = new GridFsResource(id, gridFSDownloadStream);
             //获取流中的数据
             try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
             } catch (IOException e) {
                e.printStackTrace();
             }
         }
         return null;
     }
     //获取页面模型数据
     public Map getModelByPageId(String pageId){
         //查询页面信息
         Optional<CmsPage> optional = this.cmsPageRepository.findById(pageId);
         if (!optional.isPresent()) {
             //页面不存在
             ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
         }
         CmsPage cmsPage = (CmsPage) optional.get();
         //取出dataUrl
         String dataUrl = cmsPage.getDataUrl();
         if(StringUtils.isEmpty(dataUrl)){
             ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
         }
         ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
         Map body = forEntity.getBody();
         return body;
     }
}

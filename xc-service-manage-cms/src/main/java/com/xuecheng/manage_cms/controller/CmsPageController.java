package com.xuecheng.manage_cms.controller;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author gakki
 */
@RestController
@RequestMapping("/cms/page")
@Api(value="cms页面管理接口",description="cms页面管理接口，提供页面的增、删、改、查")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    CmsPageService cmsPageService;

    @Override
    @GetMapping("/list/{page}/{size}")
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value="页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value="每页记录数",required=true,paramType="path",dataType="int")
            })
    public QueryResponseResult findList(@PathVariable("page") int page,@PathVariable("size") int size,QueryPageRequest queryPageRequest) {

        return cmsPageService.findList(page, size, queryPageRequest);
    }

    @Override
    @PostMapping("/add")
    @ApiOperation("新增页面")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return this.cmsPageService.add(cmsPage);
    }

    @Override
    @PutMapping("/update/{id}")
    public CmsPageResult update(@PathVariable("id") String id,@RequestBody CmsPage cmsPage) {
        return this.cmsPageService.update(id,cmsPage);
    }

    @Override
    @GetMapping("get/{id}")
    public CmsPageResult findById(@PathVariable("id") String id) {
        CmsPage cmsPage = this.cmsPageService.findById(id);
        if (cmsPage == null) {
            return new CmsPageResult(CommonCode.FAIL, null);
        }
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    @Override
    @DeleteMapping("delete/{id}")
    public ResponseResult delete(@PathVariable String id) {
        return this.cmsPageService.delete(id);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return this.cmsPageService.postPage(pageId);
    }

    @Override
    @PostMapping("save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return this.cmsPageService.save(cmsPage);
    }

    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return this.cmsPageService.postPageQuick(cmsPage);
    }
}

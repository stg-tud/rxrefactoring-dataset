/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.solo;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.Collection;
import java.util.Locale;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.ioc.config.Discoverer;
import org.b3log.solo.repository.ArchiveDateArticleRepository;
import org.b3log.solo.repository.ArchiveDateRepository;
import org.b3log.solo.repository.ArticleRepository;
import org.b3log.solo.repository.CommentRepository;
import org.b3log.solo.repository.LinkRepository;
import org.b3log.solo.repository.OptionRepository;
import org.b3log.solo.repository.PageRepository;
import org.b3log.solo.repository.PluginRepository;
import org.b3log.solo.repository.PreferenceRepository;
import org.b3log.solo.repository.StatisticRepository;
import org.b3log.solo.repository.TagArticleRepository;
import org.b3log.solo.repository.TagRepository;
import org.b3log.solo.repository.UserRepository;
import org.b3log.solo.repository.impl.ArchiveDateArticleRepositoryImpl;
import org.b3log.solo.repository.impl.ArchiveDateRepositoryImpl;
import org.b3log.solo.repository.impl.ArticleRepositoryImpl;
import org.b3log.solo.repository.impl.CommentRepositoryImpl;
import org.b3log.solo.repository.impl.LinkRepositoryImpl;
import org.b3log.solo.repository.impl.OptionRepositoryImpl;
import org.b3log.solo.repository.impl.PageRepositoryImpl;
import org.b3log.solo.repository.impl.PluginRepositoryImpl;
import org.b3log.solo.repository.impl.PreferenceRepositoryImpl;
import org.b3log.solo.repository.impl.StatisticRepositoryImpl;
import org.b3log.solo.repository.impl.TagArticleRepositoryImpl;
import org.b3log.solo.repository.impl.TagRepositoryImpl;
import org.b3log.solo.repository.impl.UserRepositoryImpl;
import org.b3log.solo.service.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Abstract test case.
 * 
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.8, Oct 14, 2013
 * @see #beforeClass() 
 * @see #afterClass() 
 */
public abstract class AbstractTestCase {

    /**
     * Local service test helper.
     */
    private final LocalServiceTestHelper localServiceTestHelper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    /**
     * Bean manager.
     */
    private LatkeBeanManager beanManager;

    /**
     * Before class.
     * 
     * <ol>
     *   <li>Sets up GAE unit test runtime environment</li>
     *   <li>Initializes Latke runtime</li>
     *   <li>Instantiates repositories</li>
     * </ol>
     * @throws Exception exception
     */
    @BeforeClass
    public void beforeClass() throws Exception {
        localServiceTestHelper.setUp();

        Latkes.initRuntimeEnv();
        Latkes.setLocale(Locale.SIMPLIFIED_CHINESE);

        final Collection<Class<?>> classes = Discoverer.discover("org.b3log.solo");
        Lifecycle.startApplication(classes);

        beanManager = Lifecycle.getBeanManager();
    }

    /**
     * After class.
     * 
     * <ol>
     *   <li>Tears down GAE unit test runtime environment</li>
     *   <li>Shutdowns Latke runtime</li>
     * </ol>
     */
    @AfterClass
    public void afterClass() {
        // XXX: NPE, localServiceTestHelper.tearDown();

        Latkes.shutdown();
    }

    /**
     * Gets user repository.
     * 
     * @return user repository
     */
    public UserRepository getUserRepository() {
        return beanManager.getReference(UserRepositoryImpl.class);
    }

    /**
     * Gets link repository.
     * 
     * @return link repository
     */
    public LinkRepository getLinkRepository() {
        return beanManager.getReference(LinkRepositoryImpl.class);
    }

    /**
     * Gets article repository.
     * 
     * @return article repository
     */
    public ArticleRepository getArticleRepository() {
        return beanManager.getReference(ArticleRepositoryImpl.class);
    }

    /**
     * Gets tag repository.
     * 
     * @return tag repository
     */
    public TagRepository getTagRepository() {
        return beanManager.getReference(TagRepositoryImpl.class);
    }

    /**
     * Gets tag-article repository.
     * 
     * @return tag-article repository
     */
    public TagArticleRepository getTagArticleRepository() {
        return beanManager.getReference(TagArticleRepositoryImpl.class);
    }

    /**
     * Gets page repository.
     * 
     * @return page repository
     */
    public PageRepository getPageRepository() {
        return beanManager.getReference(PageRepositoryImpl.class);
    }

    /**
     * Gets comment repository.
     * 
     * @return comment repository
     */
    public CommentRepository getCommentRepository() {
        return beanManager.getReference(CommentRepositoryImpl.class);
    }

    /**
     * Gets archive date repository.
     * 
     * @return archive date repository
     */
    public ArchiveDateRepository getArchiveDateRepository() {
        return beanManager.getReference(ArchiveDateRepositoryImpl.class);
    }

    /**
     * Archive date article repository.
     * 
     * @return archive date article repository
     */
    public ArchiveDateArticleRepository getArchiveDateArticleRepository() {
        return beanManager.getReference(ArchiveDateArticleRepositoryImpl.class);
    }

    /**
     * Gets plugin repository.
     * 
     * @return plugin repository
     */
    public PluginRepository getPluginRepository() {
        return beanManager.getReference(PluginRepositoryImpl.class);
    }

    /**
     * Gets preference repository.
     * 
     * @return preference repository
     */
    public PreferenceRepository getPreferenceRepository() {
        return beanManager.getReference(PreferenceRepositoryImpl.class);
    }

    /**
     * Gets statistic repository.
     * 
     * @return statistic repository
     */
    public StatisticRepository getStatisticRepository() {
        return beanManager.getReference(StatisticRepositoryImpl.class);
    }

    /**
     * Gets option repository.
     * 
     * @return option repository
     */
    public OptionRepository getOptionRepository() {
        return beanManager.getReference(OptionRepositoryImpl.class);
    }

    /**
     * Gets initialization service.
     * 
     * @return initialization service
     */
    public InitService getInitService() {
        return beanManager.getReference(InitService.class);
    }

    /**
     * Gets user management service.
     * 
     * @return user management service
     */
    public UserMgmtService getUserMgmtService() {
        return beanManager.getReference(UserMgmtService.class);
    }

    /**
     * Gets user query service.
     * 
     * @return user query service
     */
    public UserQueryService getUserQueryService() {
        return beanManager.getReference(UserQueryService.class);
    }

    /**
     * Gets article management service.
     * 
     * @return article management service
     */
    public ArticleMgmtService getArticleMgmtService() {
        return beanManager.getReference(ArticleMgmtService.class);
    }

    /**
     * Gets article query service.
     * 
     * @return article query service
     */
    public ArticleQueryService getArticleQueryService() {
        return beanManager.getReference(ArticleQueryService.class);
    }

    /**
     * Gets page management service.
     * 
     * @return page management service
     */
    public PageMgmtService getPageMgmtService() {
        return beanManager.getReference(PageMgmtService.class);
    }

    /**
     * Gets page query service.
     * 
     * @return page query service
     */
    public PageQueryService getPageQueryService() {
        return beanManager.getReference(PageQueryService.class);
    }

    /**
     * Gets link management service.
     * 
     * @return link management service
     */
    public LinkMgmtService getLinkMgmtService() {
        return beanManager.getReference(LinkMgmtService.class);
    }

    /**
     * Gets link query service.
     *
     * @return link query service 
     */
    public LinkQueryService getLinkQueryService() {
        return beanManager.getReference(LinkQueryService.class);
    }

    /**
     * Gets preference management service.
     * 
     * @return preference management service
     */
    public PreferenceMgmtService getPreferenceMgmtService() {
        return beanManager.getReference(PreferenceMgmtService.class);
    }

    /**
     * Gets preference query service.
     * 
     * @return preference query service
     */
    public PreferenceQueryService getPreferenceQueryService() {
        return beanManager.getReference(PreferenceQueryService.class);
    }

    /**
     * Gets tag query service.
     * 
     * @return tag query service
     */
    public TagQueryService getTagQueryService() {
        return beanManager.getReference(TagQueryService.class);
    }

    /**
     * Gets tag management service.
     * 
     * @return tag management service
     */
    public TagMgmtService getTagMgmtService() {
        return beanManager.getReference(TagMgmtService.class);
    }

    /**
     * Gets comment query service.
     * 
     * @return comment query service
     */
    public CommentQueryService getCommentQueryService() {
        return beanManager.getReference(CommentQueryService.class);
    }

    /**
     * Gets comment management service.
     * 
     * @return comment management service
     */
    public CommentMgmtService getCommentMgmtService() {
        return beanManager.getReference(CommentMgmtService.class);
    }

    /**
     * Gets archive date query service.
     * 
     * @return archive date query service
     */
    public ArchiveDateQueryService getArchiveDateQueryService() {
        return beanManager.getReference(ArchiveDateQueryService.class);
    }

    /**
     * Gets option management service.
     * 
     * @return option management service
     */
    public OptionMgmtService getOptionMgmtService() {
        return beanManager.getReference(OptionMgmtService.class);
    }

    /**
     * Gets option query service.
     * 
     * @return option query service
     */
    public OptionQueryService getOptionQueryService() {
        return beanManager.getReference(OptionQueryService.class);
    }
}

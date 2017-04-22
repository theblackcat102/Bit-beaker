package fi.iki.kuitsi.bitbeaker.data.api;

import android.support.annotation.Nullable;

import java.util.Map;

import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DirectoryContent;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItems;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetComment;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueComment;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.domainobjects.Privilege;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequest;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequestComment;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.BranchNames;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Changesets;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.IssueFilterResult;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.MainBranch;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.PullRequestsResponse;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryEvents;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryFollowers;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositorySearch;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.WikiPage;
import fi.iki.kuitsi.bitbeaker.network.response.user.UserEndpoint;
import fi.iki.kuitsi.bitbeaker.network.response.users.AccountProfile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 *  * Bitbucket Cloud REST API.
 */
public interface BitbucketService {
	String BASE_URL = "https://bitbucket.org/api/";

	///////////////////////////////////////////////////////////////////
	// Repositories endpoint

	@GET("1.0/repositories/{accountname}/{repo_slug}")
	SpiceCall<Repository> repository(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug);

	@GET("1.0/repositories")
	SpiceCall<RepositorySearch> repositorySearch(
			@Query("name") String query);

	// changesets resource
	@GET("1.0/repositories/{accountname}/{repo_slug}/changesets")
	SpiceCall<Changesets> changesets(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Query("start") Object start,
			@Query("limit") Object limit);

	@GET("1.0/repositories/{accountname}/{repo_slug}/changesets/{node}")
	SpiceCall<Changeset> changeset(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("node") String node);

	@GET("1.0/repositories/{accountname}/{repo_slug}/changesets/{node}/comments")
	SpiceCall<ChangesetComment.List> changesetComment(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("node") String node);

	@FormUrlEncoded
	@POST("1.0/repositories/{accountname}/{repo_slug}/changesets/{node}/comments")
	SpiceCall<Void> postChangesetComment(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("node") String node,
			@Field("content") CharSequence content);

	// downloads resource
	@GET("2.0/repositories/{owner}/{repo_slug}/downloads")
	Call<DownloadableItems> download(
			@Path("owner") String owner,
			@Path("repo_slug") String repoSlug);

	@GET
	@Streaming
	Call<ResponseBody> downloadFile(@Url String url);

	// event resource
	@GET("1.0/repositories/{owner}/{repo}/events")
	Call<RepositoryEvents> repositoryEvents(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Query("start") Object start,
			@Query("limit") Object limit,
			@Query("type") Object type);

	@GET("1.0/repositories/{owner}/{repo}/events")
	SpiceCall<RepositoryEvents> repositoryEventsRobospice(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Query("start") Object start,
			@Query("limit") Object limit,
			@Query("type") Object type);

	// followers resource
	@GET("1.0/repositories/{owner}/{slug}/followers")
	SpiceCall<RepositoryFollowers> repositoryFollowers(
			@Path("owner") String owner,
			@Path("slug") String slug);

	// issues resource
	@GET("1.0/repositories/{accountname}/{repo_slug}/issues")
	SpiceCall<IssueFilterResult> issues(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Query("limit") Integer limit,
			@Query("start") Integer start,
			@Query("search") String search,
			@Query("status") Iterable<String> status,
			@Query("kind") Iterable<String> kind,
			@QueryMap Map<String, String> filter);

	@GET("1.0/repositories/{owner}/{slug}/issues/{issueId}")
	SpiceCall<Issue> singleIssue(
			@Path("owner") String owner,
			@Path("slug") String slug,
			@Path("issueId") int issueId);

	@GET("1.0/repositories/{accountname}/{repo_slug}/issues/{issue_id}/comments")
	SpiceCall<IssueComment.List> issueComments(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Path("issue_id") int issueId);

	@FormUrlEncoded
	@POST("1.0/repositories/{accountname}/{repo_slug}/issues")
	SpiceCall<Issue> newIssue(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@FieldMap Map<String, String> fields);

	@FormUrlEncoded
	@PUT("1.0/repositories/{accountname}/{repo_slug}/issues/{issue_id}")
	SpiceCall<Issue> updateIssue(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Path("issue_id") int issueId,
			@FieldMap Map<String, String> params);

	@FormUrlEncoded
	@POST("1.0/repositories/{accountname}/{repo_slug}/issues/{issue_id}/comments")
	SpiceCall<Void> newIssueComment(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Path("issue_id") int issueId,
			@Field("content") CharSequence content);

	@GET("1.0/repositories/{accountname}/{repo_slug}/issues/{type}s")
	SpiceCall<IssueContainer.List> issueContainers(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Path("type") @IssueContainer.Type String type);

	@FormUrlEncoded
	@POST("1.0/repositories/{accountname}/{repo_slug}/issues/{type}s")
	SpiceCall<IssueContainer> newIssueContainer(
			@Path("accountname") String accountName,
			@Path("repo_slug") String repoSlug,
			@Path("type") @IssueContainer.Type String type,
			@Field("name") String name);

	// pullrequests resource
	@GET("2.0/repositories/{accountname}/{repo_slug}/pullrequests")
	SpiceCall<PullRequestsResponse> pullrequests(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Query("state") PullRequest.State state,
			@Query("page") Integer page);

	@GET("1.0/repositories/{accountname}/{repo_slug}/pullrequests/{pull_request_id}/comments")
	SpiceCall<PullRequestComment.List> pullRequestComments(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("pull_request_id") int pullRequestId);

	@FormUrlEncoded
	@POST("1.0/repositories/{accountname}/{repo_slug}/pullrequests/{pull_request_id}/comments")
	SpiceCall<Void> postPullRequestComment(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("pull_request_id") int pullRequestId,
			@Field("content") CharSequence content);

	// repository resource
	@GET("1.0/repositories/{accountname}/{repo_slug}/branches")
	SpiceCall<BranchNames> branches(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug);

	@GET("1.0/repositories/{accountname}/{repo_slug}/main-branch")
	SpiceCall<MainBranch> mainBranch(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug);

	// src resource
	@GET("1.0/repositories/{accountname}/{repo_slug}/src/{revision}{path}")
	Call<DirectoryContent> repoSource(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("revision") String revision,
			@Path("path") String path);

	@GET("1.0/repositories/{accountname}/{repo_slug}/raw/{revision}/{path}")
	SpiceCall<String> fileContent(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("revision") String revision,
			@Path("path") String path);

	@GET("1.0/repositories/{accountname}/{repo_slug}/raw/{revision}/{path}")
	@Streaming
	Call<ResponseBody> fileContentStream(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("revision") String revision,
			@Path("path") String path);

	// wiki page resource
	@GET("1.0/repositories/{accountname}/{repo_slug}/wiki/{page}")
	SpiceCall<WikiPage> wikiPage(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("page") String page);

	// tags resource
	@GET("1.0/repositories/{owner}/{slug}/tags")
	SpiceCall<Tags> tags(
			@Path("owner") String owner,
			@Path("slug") String slug);

	// diff resource
	@GET("2.0/repositories/{accountname}/{repo_slug}/diff/{changeset_id}")
	Call<String> diff(
			@Path("accountname") String accountname,
			@Path("repo_slug") String slug,
			@Path("changeset_id") String changeset_id,
			@Query("path") @Nullable String path);

	///////////////////////////////////////////////////////////////////
	// User endpoint

	@GET("1.0/user")
	SpiceCall<UserEndpoint> user_api_v1();

	@GET("2.0/user")
	Call<User> user();

	@GET("1.0/user/follows")
	SpiceCall<Repository.List> userFollow();

	@GET("1.0/user/repositories")
	SpiceCall<Repository.List> userRepositories();

	///////////////////////////////////////////////////////////////////
	// Users endpoint

	// account resource
	@GET("1.0/users/{accountname}")
	SpiceCall<AccountProfile> accountProfile(
			@Path("accountname") String accountname);

	@GET("1.0/privileges/{owner}/{repo}/{privilegeaccount}")
	SpiceCall<Privilege.List> privilege(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Path("privilegeaccount") String privilegeAccount);

	@DELETE("1.0/privileges/{accountname}/{repo_slug}/{privilege_account}")
	Call<Void> deleteAccountPrivilegesFromRepository(
			@Path("accountname") String account,
			@Path("repo_slug") String repoSlug,
			@Path("privilege_account") String privilegeAccount);
}

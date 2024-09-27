package groups

import (
	"context"
	i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543 "github.com/apicurio/apicurio-registry/go-sdk/pkg/registryclient-v2/models"
	i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f "github.com/microsoft/kiota-abstractions-go"
)

// ItemArtifactsItemVersionsItemCommentsRequestBuilder manage a collection of comments for an artifact version
type ItemArtifactsItemVersionsItemCommentsRequestBuilder struct {
	i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.BaseRequestBuilder
}

// ItemArtifactsItemVersionsItemCommentsRequestBuilderGetRequestConfiguration configuration for the request such as headers, query parameters, and middleware options.
type ItemArtifactsItemVersionsItemCommentsRequestBuilderGetRequestConfiguration struct {
	// Request headers
	Headers *i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestHeaders
	// Request options
	Options []i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestOption
}

// ItemArtifactsItemVersionsItemCommentsRequestBuilderPostRequestConfiguration configuration for the request such as headers, query parameters, and middleware options.
type ItemArtifactsItemVersionsItemCommentsRequestBuilderPostRequestConfiguration struct {
	// Request headers
	Headers *i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestHeaders
	// Request options
	Options []i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestOption
}

// ByCommentId manage a single comment
// returns a *ItemArtifactsItemVersionsItemCommentsWithCommentItemRequestBuilder when successful
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) ByCommentId(commentId string) *ItemArtifactsItemVersionsItemCommentsWithCommentItemRequestBuilder {
	urlTplParams := make(map[string]string)
	for idx, item := range m.BaseRequestBuilder.PathParameters {
		urlTplParams[idx] = item
	}
	if commentId != "" {
		urlTplParams["commentId"] = commentId
	}
	return NewItemArtifactsItemVersionsItemCommentsWithCommentItemRequestBuilderInternal(urlTplParams, m.BaseRequestBuilder.RequestAdapter)
}

// NewItemArtifactsItemVersionsItemCommentsRequestBuilderInternal instantiates a new ItemArtifactsItemVersionsItemCommentsRequestBuilder and sets the default values.
func NewItemArtifactsItemVersionsItemCommentsRequestBuilderInternal(pathParameters map[string]string, requestAdapter i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestAdapter) *ItemArtifactsItemVersionsItemCommentsRequestBuilder {
	m := &ItemArtifactsItemVersionsItemCommentsRequestBuilder{
		BaseRequestBuilder: *i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.NewBaseRequestBuilder(requestAdapter, "{+baseurl}/groups/{groupId}/artifacts/{artifactId}/versions/{version}/comments", pathParameters),
	}
	return m
}

// NewItemArtifactsItemVersionsItemCommentsRequestBuilder instantiates a new ItemArtifactsItemVersionsItemCommentsRequestBuilder and sets the default values.
func NewItemArtifactsItemVersionsItemCommentsRequestBuilder(rawUrl string, requestAdapter i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestAdapter) *ItemArtifactsItemVersionsItemCommentsRequestBuilder {
	urlParams := make(map[string]string)
	urlParams["request-raw-url"] = rawUrl
	return NewItemArtifactsItemVersionsItemCommentsRequestBuilderInternal(urlParams, requestAdapter)
}

// Get retrieves all comments for a version of an artifact.  Both the `artifactId` and theunique `version` number must be provided.This operation can fail for the following reasons:* No artifact with this `artifactId` exists (HTTP error `404`)* No version with this `version` exists (HTTP error `404`)* A server error occurred (HTTP error `500`)
// returns a []Commentable when successful
// returns a Error error when the service returns a 404 status code
// returns a Error error when the service returns a 500 status code
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) Get(ctx context.Context, requestConfiguration *ItemArtifactsItemVersionsItemCommentsRequestBuilderGetRequestConfiguration) ([]i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.Commentable, error) {
	requestInfo, err := m.ToGetRequestInformation(ctx, requestConfiguration)
	if err != nil {
		return nil, err
	}
	errorMapping := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.ErrorMappings{
		"404": i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateErrorFromDiscriminatorValue,
		"500": i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateErrorFromDiscriminatorValue,
	}
	res, err := m.BaseRequestBuilder.RequestAdapter.SendCollection(ctx, requestInfo, i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateCommentFromDiscriminatorValue, errorMapping)
	if err != nil {
		return nil, err
	}
	val := make([]i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.Commentable, len(res))
	for i, v := range res {
		if v != nil {
			val[i] = v.(i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.Commentable)
		}
	}
	return val, nil
}

// Post adds a new comment to the artifact version.  Both the `artifactId` and theunique `version` number must be provided.This operation can fail for the following reasons:* No artifact with this `artifactId` exists (HTTP error `404`)* No version with this `version` exists (HTTP error `404`)* A server error occurred (HTTP error `500`)
// returns a Commentable when successful
// returns a Error error when the service returns a 404 status code
// returns a Error error when the service returns a 500 status code
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) Post(ctx context.Context, body i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.DTONewCommentable, requestConfiguration *ItemArtifactsItemVersionsItemCommentsRequestBuilderPostRequestConfiguration) (i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.Commentable, error) {
	requestInfo, err := m.ToPostRequestInformation(ctx, body, requestConfiguration)
	if err != nil {
		return nil, err
	}
	errorMapping := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.ErrorMappings{
		"404": i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateErrorFromDiscriminatorValue,
		"500": i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateErrorFromDiscriminatorValue,
	}
	res, err := m.BaseRequestBuilder.RequestAdapter.Send(ctx, requestInfo, i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.CreateCommentFromDiscriminatorValue, errorMapping)
	if err != nil {
		return nil, err
	}
	if res == nil {
		return nil, nil
	}
	return res.(i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.Commentable), nil
}

// ToGetRequestInformation retrieves all comments for a version of an artifact.  Both the `artifactId` and theunique `version` number must be provided.This operation can fail for the following reasons:* No artifact with this `artifactId` exists (HTTP error `404`)* No version with this `version` exists (HTTP error `404`)* A server error occurred (HTTP error `500`)
// returns a *RequestInformation when successful
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) ToGetRequestInformation(ctx context.Context, requestConfiguration *ItemArtifactsItemVersionsItemCommentsRequestBuilderGetRequestConfiguration) (*i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestInformation, error) {
	requestInfo := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.NewRequestInformationWithMethodAndUrlTemplateAndPathParameters(i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.GET, m.BaseRequestBuilder.UrlTemplate, m.BaseRequestBuilder.PathParameters)
	if requestConfiguration != nil {
		requestInfo.Headers.AddAll(requestConfiguration.Headers)
		requestInfo.AddRequestOptions(requestConfiguration.Options)
	}
	requestInfo.Headers.TryAdd("Accept", "application/json")
	return requestInfo, nil
}

// ToPostRequestInformation adds a new comment to the artifact version.  Both the `artifactId` and theunique `version` number must be provided.This operation can fail for the following reasons:* No artifact with this `artifactId` exists (HTTP error `404`)* No version with this `version` exists (HTTP error `404`)* A server error occurred (HTTP error `500`)
// returns a *RequestInformation when successful
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) ToPostRequestInformation(ctx context.Context, body i80228d093fd3b582ec81b86f113cc707692a60cdd08bae7a390086a8438c7543.DTONewCommentable, requestConfiguration *ItemArtifactsItemVersionsItemCommentsRequestBuilderPostRequestConfiguration) (*i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestInformation, error) {
	requestInfo := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.NewRequestInformationWithMethodAndUrlTemplateAndPathParameters(i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.POST, m.BaseRequestBuilder.UrlTemplate, m.BaseRequestBuilder.PathParameters)
	if requestConfiguration != nil {
		requestInfo.Headers.AddAll(requestConfiguration.Headers)
		requestInfo.AddRequestOptions(requestConfiguration.Options)
	}
	requestInfo.Headers.TryAdd("Accept", "application/json")
	err := requestInfo.SetContentFromParsable(ctx, m.BaseRequestBuilder.RequestAdapter, "application/json", body)
	if err != nil {
		return nil, err
	}
	return requestInfo, nil
}

// WithUrl returns a request builder with the provided arbitrary URL. Using this method means any other path or query parameters are ignored.
// returns a *ItemArtifactsItemVersionsItemCommentsRequestBuilder when successful
func (m *ItemArtifactsItemVersionsItemCommentsRequestBuilder) WithUrl(rawUrl string) *ItemArtifactsItemVersionsItemCommentsRequestBuilder {
	return NewItemArtifactsItemVersionsItemCommentsRequestBuilder(rawUrl, m.BaseRequestBuilder.RequestAdapter)
}
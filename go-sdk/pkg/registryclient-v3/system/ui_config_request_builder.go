package system

import (
	"context"
	iefa8953a3555be741841d5395d25b8cc91d8ea997e2cc98794b61191090ff773 "github.com/apicurio/apicurio-registry/go-sdk/v3/pkg/registryclient-v3/models"
	i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f "github.com/microsoft/kiota-abstractions-go"
)

// UiConfigRequestBuilder this endpoint is used by the user interface to retrieve UI specific configurationin a JSON payload.  This allows the UI and the backend to be configured in the same place (the backend process/pod).  When the UI loads, it will make an API callto this endpoint to determine what UI features and options are configured.
type UiConfigRequestBuilder struct {
	i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.BaseRequestBuilder
}

// UiConfigRequestBuilderGetRequestConfiguration configuration for the request such as headers, query parameters, and middleware options.
type UiConfigRequestBuilderGetRequestConfiguration struct {
	// Request headers
	Headers *i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestHeaders
	// Request options
	Options []i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestOption
}

// NewUiConfigRequestBuilderInternal instantiates a new UiConfigRequestBuilder and sets the default values.
func NewUiConfigRequestBuilderInternal(pathParameters map[string]string, requestAdapter i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestAdapter) *UiConfigRequestBuilder {
	m := &UiConfigRequestBuilder{
		BaseRequestBuilder: *i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.NewBaseRequestBuilder(requestAdapter, "{+baseurl}/system/uiConfig", pathParameters),
	}
	return m
}

// NewUiConfigRequestBuilder instantiates a new UiConfigRequestBuilder and sets the default values.
func NewUiConfigRequestBuilder(rawUrl string, requestAdapter i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestAdapter) *UiConfigRequestBuilder {
	urlParams := make(map[string]string)
	urlParams["request-raw-url"] = rawUrl
	return NewUiConfigRequestBuilderInternal(urlParams, requestAdapter)
}

// Get returns the UI configuration properties for this server.  The registry UI can beconnected to a backend using just a URL.  The rest of the UI configuration can thenbe fetched from the backend using this operation.  This allows UI and backend toboth be configured in the same place.This operation may fail for one of the following reasons:* A server error occurred (HTTP error `500`)
// returns a UserInterfaceConfigable when successful
// returns a ProblemDetails error when the service returns a 500 status code
func (m *UiConfigRequestBuilder) Get(ctx context.Context, requestConfiguration *UiConfigRequestBuilderGetRequestConfiguration) (iefa8953a3555be741841d5395d25b8cc91d8ea997e2cc98794b61191090ff773.UserInterfaceConfigable, error) {
	requestInfo, err := m.ToGetRequestInformation(ctx, requestConfiguration)
	if err != nil {
		return nil, err
	}
	errorMapping := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.ErrorMappings{
		"500": iefa8953a3555be741841d5395d25b8cc91d8ea997e2cc98794b61191090ff773.CreateProblemDetailsFromDiscriminatorValue,
	}
	res, err := m.BaseRequestBuilder.RequestAdapter.Send(ctx, requestInfo, iefa8953a3555be741841d5395d25b8cc91d8ea997e2cc98794b61191090ff773.CreateUserInterfaceConfigFromDiscriminatorValue, errorMapping)
	if err != nil {
		return nil, err
	}
	if res == nil {
		return nil, nil
	}
	return res.(iefa8953a3555be741841d5395d25b8cc91d8ea997e2cc98794b61191090ff773.UserInterfaceConfigable), nil
}

// ToGetRequestInformation returns the UI configuration properties for this server.  The registry UI can beconnected to a backend using just a URL.  The rest of the UI configuration can thenbe fetched from the backend using this operation.  This allows UI and backend toboth be configured in the same place.This operation may fail for one of the following reasons:* A server error occurred (HTTP error `500`)
// returns a *RequestInformation when successful
func (m *UiConfigRequestBuilder) ToGetRequestInformation(ctx context.Context, requestConfiguration *UiConfigRequestBuilderGetRequestConfiguration) (*i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.RequestInformation, error) {
	requestInfo := i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.NewRequestInformationWithMethodAndUrlTemplateAndPathParameters(i2ae4187f7daee263371cb1c977df639813ab50ffa529013b7437480d1ec0158f.GET, m.BaseRequestBuilder.UrlTemplate, m.BaseRequestBuilder.PathParameters)
	if requestConfiguration != nil {
		requestInfo.Headers.AddAll(requestConfiguration.Headers)
		requestInfo.AddRequestOptions(requestConfiguration.Options)
	}
	requestInfo.Headers.TryAdd("Accept", "application/json")
	return requestInfo, nil
}

// WithUrl returns a request builder with the provided arbitrary URL. Using this method means any other path or query parameters are ignored.
// returns a *UiConfigRequestBuilder when successful
func (m *UiConfigRequestBuilder) WithUrl(rawUrl string) *UiConfigRequestBuilder {
	return NewUiConfigRequestBuilder(rawUrl, m.BaseRequestBuilder.RequestAdapter)
}

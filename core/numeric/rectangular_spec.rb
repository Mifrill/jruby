require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../shared/rect', __FILE__)

describe "Numeric#rectangular" do
  it_behaves_like(:numeric_rect, :rectangular)
end

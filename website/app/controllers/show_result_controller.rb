class ShowResultController < ApplicationController

  def index
    @region_labels = RegionLabel.load_from_file
    RegionLabel.check_l2r(@region_labels)
    Region.check_labeled_images
    @original_images = load_original_images
    @labeled_images = load_labled_images
  end

  private
  def check_l2r(force = false)
  end

  def load_original_images
  end

  def load_labeled_images(force = false)
  end


end

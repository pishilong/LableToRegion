class ShowResultController < ApplicationController

  def index
    @region_labels = RegionLabel.load_from_file
    RegionLabel.check_l2r(@region_labels, false, false)
    RegionLabel.check_labeled_images(@region_labels, false, false)
    @original_images = load_original_images
    @labeled_images = load_labled_images
  end

  private
  def load_original_images
    dir_path = File.join(Rails.root, 'app', 'assets', 'images', 'origin')
    Dir.foreach(dir_path).reject{|x| File.basename(x).in?([".", ".."])}.sort_by{|x| x.split('.')[0].to_i}.map{|x| "origin/" + x}
  end
  def load_labled_images
    dir_path = File.join(Rails.root, 'app', 'assets', 'images', 'labled')
    Dir.foreach(dir_path).select{|x| File.basename(x) =~ /\.jpg/}.sort_by{|x| x.split('.')[0].to_i}.map{|x| "labled/" + x}
  end
end

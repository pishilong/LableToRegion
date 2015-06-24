class RegionLabel
  attr_accessor :image_id
  attr_accessor :region_id
  attr_accessor :label_id

  class << self
    def load_from_file
      region_labels = []
      File.open(File.join(Rails.root, 'public', 'regionLabel', 'regionLabel.his')) do |file|
        while line = file.gets
          region_label = RegionLabel.new
          elements = line.split(" ")
          region_label.image_id = elements[0].split(",")[0].to_i
          region_label.region_id = elements[0].split(",")[1].to_i
          region_label.label_id = elements.last.to_i
          region_labels << region_label
        end
      end

      region_labels
    end

    def check_l2r(region_labels, force = false)
      image_ids = region_labels.map{|x| x.image_id}.sort.uniq
      puts image_ids

      image_ids.each do |image_id|
        puts "Processing image #{image_id}"
        next if File.exist?(File.join(Rails.root, 'public', 'regionLabel', "#{image_id}.l2r")) && !force

        image_region_labels = region_labels.select{|rl| rl.image_id == image_id}
        mask_file = File.read(File.join(Rails.root, 'public', 'mask', "#{image_id}.mask"))
        l2r_file = File.open(File.join(Rails.root, 'public', 'regionLabel', "#{image_id}.l2r"), "w")
        image_region_labels.each do |rl|
          mask_file.gsub!(rl.region_id.to_s, rl.label_id.to_s)
        end
        l2r_file << mask_file
        l2r_file.close
      end
    end

  end
end

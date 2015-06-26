class RegionLabel
  attr_accessor :image_id
  attr_accessor :region_id
  attr_accessor :label_id
  COLOR_SETTING = {"-1" => "190 190 190",
                   "0" => "255 255 0",
                   "1" => "0 255 0",
                   "2" => "0 0 255",
                   "3" => "221 160 221",
                   "4" => "139 0 0",
                   "5" => "223 68 55",
                   "6" => "255 222 173",
                   "7" => "255 250 250"
  }
  LABEL_TEXT = {
    "-1" => "Unknown",
    "0" => "Sky",
    "1" => "Tree",
    "2" => "Road",
    "3" => "Grass",
    "4" => "Water",
    "5" => "Building",
    "6" => "Mountain",
    "7" => "foreground"
  }

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

    def check_l2r(region_labels, force = false, dummy = false)
      image_ids = region_labels.map{|x| x.image_id}.sort.uniq
      image_ids = (1 .. 5).to_a if dummy

      image_ids.each do |image_id|
        puts "Processing image #{image_id}"
        next if File.exist?(File.join(Rails.root, 'public', 'regionLabel', "#{image_id}.l2r")) && !force

        image_region_labels = region_labels.select{|rl| rl.image_id == image_id}.sort_by{|x| x.region_id.to_i}.reverse
        mask_file = File.read(File.join(Rails.root, 'public', 'mask', "#{image_id}.mask"))
        l2r_file = File.open(File.join(Rails.root, 'public', 'regionLabel', "#{image_id}.l2r"), "w")
        buffer = []
        mask_file.split("\n").each do |line|
          image_region_labels.each do |rl|
            line = line.split(" ").map{|x|  x == rl.region_id.to_s ? "region_#{rl.region_id.to_s}" : x}.join(" ")
          end
          buffer << line
          buffer << "\n"
        end
        buffer = buffer.join("")
        image_region_labels.each do |rl|
          buffer.gsub!("region_#{rl.region_id.to_s}", rl.label_id.to_s )
        end
        l2r_file << buffer
        l2r_file.close
      end
    end

    def check_labeled_images(region_labels, force = false, dummy = false)

      image_ids = region_labels.map{|x| x.image_id}.sort.uniq
      image_ids = (1 .. 5).to_a if dummy

      image_ids.each do |image_id|
        puts "Processing image #{image_id}"
        ppm_file_path = File.join(Rails.root, 'app', 'assets', 'images', 'labled', "#{image_id}.ppm")
        next if File.exist?(ppm_file_path) && !force
        l2r_file = File.read(File.join(Rails.root, 'public', 'regionLabel', "#{image_id}.l2r"))
        row_count = l2r_file.split("\n").size
        column_count = l2r_file.split("\n")[0].split(" ").size
        RegionLabel::COLOR_SETTING.each do |label_id, color|
          l2r_file.gsub!(label_id, "label_#{label_id}")
        end
        RegionLabel::COLOR_SETTING.each do |label_id, color|
          l2r_file.gsub!("label_#{label_id}", color)
        end
        File.delete(ppm_file_path) if File.exist?(ppm_file_path)
        ppm_file = File.open(ppm_file_path, "w")
        ppm_file << "P3\n"
        ppm_file << "#{column_count} #{row_count}\n"
        ppm_file << "255\n"
        ppm_file << l2r_file
        ppm_file.close
        system "convert #{File.join(Rails.root, 'app', 'assets', 'images', 'labled', "#{image_id}.ppm")} #{File.join(Rails.root, 'app', 'assets', 'images', 'labled', "#{image_id}.jpg")}"
      end
    end

  end
end

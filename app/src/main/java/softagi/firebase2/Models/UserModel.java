package softagi.firebase2.Models;

public class UserModel
{
    private String email,username,mobile,address,imageurl;

    public UserModel() {
    }

    public UserModel(String email, String username, String mobile, String address, String imageurl) {
        this.email = email;
        this.username = username;
        this.mobile = mobile;
        this.address = address;
        this.imageurl = imageurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}

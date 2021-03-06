/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pratchaya.cv.imgproc;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.cvkernels;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 *
 * @author pratchaya
 */
public class PerspectiveTransform {

    public static IplImage apply(IplImage _image, IplImage _image1) {

        //normalize image
        IplImage clonebin = cvCloneImage(_image);
        IplImage dest = cvCloneImage(_image1);
        clonebin = Grayscale.apply(clonebin);
        clonebin = Gaussian.apply(clonebin, 3);
        clonebin = Threshold.apply(clonebin, 120, 255);

        //set cornner for PerspectiveTransform
        CvPoint2D32f point = new CvPoint2D32f(4);
        CvPoint2D32f point2 = new CvPoint2D32f(4);

        int _x = 0, _y = 0;
        CvSeq contours = new CvSeq(null);
        CvMemStorage memory = CvMemStorage.create();
        cvFindContours(clonebin, memory, contours, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
        CvSeq approx;
        CvRect a;
        while (contours != null && !contours.isNull()) {

            if (contours.elem_size() > 0) {
                approx = cvApproxPoly(contours, Loader.sizeof(CvContour.class), memory, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.02, 0);
                a = cvBoundingRect(approx, 0);
                for (int i = 0; i < approx.total(); i++) {
                    if (approx.total() == 4 && a.x() != 1) {
                        CvPoint v = new CvPoint(cvGetSeqElem(approx, i));
                        point.position(i).put(v.x(), v.y());
                        //cvDrawCircle(dest, v, 6, CV_RGB(255, 0, 0), -1, 50, 0);
                        //System.out.println(" X value = " + v.x() + " ; Y value =" + v.y());
                        //cvDrawContours(dest, approx, CvScalar.ONE, CV_RGB(255, 0, 255), -1, 2, CV_AA);
                        //System.out.println(a.width() + ":" + a.height());
                        _x = a.width();
                        _y = a.height();
                    }
                }
            }
            contours = contours.h_next();
        }

        point2.position(0).put(0, 0);
        point2.position(1).put(_x, 0);
        point2.position(2).put(_x, _y);
        point2.position(3).put(0, _y);

        CvMat mat = cvCreateMat(3, 3, CV_32FC1);
        IplImage temp = cvCreateImage(cvSize(_x, _y), dest.depth(), dest.nChannels());
        cvGetPerspectiveTransform(point.position(0), point2.position(0), mat);
        cvWarpPerspective(dest, temp, mat);
        return temp;
    }
}
